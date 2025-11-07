package es.fpfempa.acd.dao;

import es.fpfempa.acd.entities.Address;
import es.fpfempa.acd.entities.Order;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class OrderDao {

    private EntityManager em;

    // Constructor: recibe el EntityManager desde Main
    public OrderDao(EntityManager em) {
        this.em = em;
    }

    /**
     * Busca un pedido por su ID (sin cargar relaciones)
     * @param orderId ID del pedido
     * @return Order o null si no existe
     */
    public Order findById(Integer orderId) {
        return em.find(Order.class, orderId);
    }

    /**
     * Busca un pedido por ID y carga Customer y Address (evita LazyInitializationException)
     * @param orderId ID del pedido
     * @return Order con relaciones cargadas o null si no existe
     */
    public Order findByIdConDetalles(Integer orderId) {
        String jpql = "SELECT o FROM Order o " +
                // Fetch porque como está en lazy, fetch le obliga a cargar customer antes de que cierre la conexión
                "JOIN FETCH o.customer " +
                "LEFT JOIN FETCH o.shippingAddress " +
                "WHERE o.id = :orderId";

        TypedQuery<Order> query = em.createQuery(jpql, Order.class);
        query.setParameter("orderId", orderId);

        try {
            return query.getSingleResult();
        } catch (Exception e) {
            return null; // Si no encuentra el pedido
        }
    }

    /**
     * Actualiza un pedido existente (para cambiar la dirección de envío)
     * Estrategia: obtener el pedido gestionado y la dirección gestionada, asignar y commit.
     */
    public void update(Order order) {
        try {
            em.getTransaction().begin();

            // 1) Pedido gestionado
            Order managedOrder = em.find(Order.class, order.getId());
            if (managedOrder == null) {
                throw new IllegalArgumentException("Pedido no encontrado id=" + order.getId());
            }

            // 2) Dirección gestionada (si viene una)
            if (order.getShippingAddress() != null && order.getShippingAddress().getId() != null) {
                Address managedAddr = em.getReference(Address.class, order.getShippingAddress().getId());
                managedOrder.setShippingAddress(managedAddr);
            } else {
                managedOrder.setShippingAddress(null);
            }

            // 3) Commit (flush implícito)
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error al actualizar el pedido: " + e.getMessage(), e);
        }
    }
}
