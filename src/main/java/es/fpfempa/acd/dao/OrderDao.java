package es.fpfempa.acd.dao;

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
     * @param order Pedido a actualizar
     */
    public void update(Order order) {
        try {
            em.getTransaction().begin();
            em.merge(order);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error al actualizar el pedido: " + e.getMessage());
        }
    }
}
