package es.fpfempa.acd.dao;

import es.fpfempa.acd.entities.Customer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class CustomerDao {

    private EntityManager em;

    public CustomerDao(EntityManager em) {
        this.em = em;
    }

    /**
     * Crea un NUEVO cliente en la BD
     * @param customer Cliente a crear
     */
    public void create(Customer customer) {
        try {
            em.getTransaction().begin();
            em.persist(customer);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error al crear el cliente: " + e.getMessage());
        }
    }

    /**
     * Busca un cliente por su ID
     * @param customerId ID del cliente
     * @return Customer o null si no existe
     */
    public Customer findById(Integer customerId) {
        return em.find(Customer.class, customerId);
    }

    /**
     * Busca un cliente por su email
     * @param email Email del cliente
     * @return Customer o null si no existe
     */
    public Customer findByEmail(String email) {
        String jpql = "SELECT c FROM Customer c WHERE c.email = :email";
        TypedQuery<Customer> query = em.createQuery(jpql, Customer.class);
        query.setParameter("email", email);
        try {
            return query.getSingleResult();
        } catch (Exception e) {
            return null;  // Si no encuentra el cliente
        }
    }

    /**
     * Busca un cliente por ID y carga sus direcciones
     * @param customerId ID del cliente
     * @return Customer con direcciones cargadas o null si no existe
     */
    public Customer findByIdWithAddresses(Integer customerId) {
        String jpql = "SELECT c FROM Customer c " +
                "LEFT JOIN FETCH c.addresses " +
                "WHERE c.id = :customerId";
        TypedQuery<Customer> query = em.createQuery(jpql, Customer.class);
        query.setParameter("customerId", customerId);
        try {
            return query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Obtiene todos los clientes
     * @return Lista de todos los clientes
     */
    public List<Customer> findAll() {
        String jpql = "SELECT c FROM Customer c";
        TypedQuery<Customer> query = em.createQuery(jpql, Customer.class);
        return query.getResultList();
    }


    /**
     * Actualiza un cliente existente
     * @param customer Cliente a actualizar
     */
    public void update(Customer customer) {
        try {
            em.getTransaction().begin();
            em.merge(customer);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error al actualizar el cliente: " + e.getMessage());
        }
    }

    /**
     * Elimina un cliente (si no tiene pedidos asociados)
     * @param customerId ID del cliente a eliminar
     */
    public void delete(Integer customerId) {
        try {
            em.getTransaction().begin();
            Customer customer = em.find(Customer.class, customerId);
            if (customer != null) {
                em.remove(customer);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error al eliminar el cliente: " + e.getMessage());
        }
    }
}
