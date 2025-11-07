package es.fpfempa.acd.dao;

import es.fpfempa.acd.entities.Address;
import es.fpfempa.acd.entities.Customer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class AddressDao {

    private EntityManager em;

    public AddressDao(EntityManager em) {
        this.em = em;
    }

    /**
     * Crea una NUEVA dirección en la BD
     * @param address Dirección a crear
     */
    public void create(Address address) {
        try {
            em.getTransaction().begin();
            em.persist(address);  // persist() porque es nueva
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error al crear la dirección: " + e.getMessage());
        }
    }

    /**
     * Busca una dirección por su ID
     * @param addressId ID de la dirección
     * @return Address o null si no existe
     */
    public Address findById(Integer addressId) {
        return em.find(Address.class, addressId);
    }

    /**
     * Busca todas las direcciones de un cliente específico
     * @param customerId ID del cliente
     * @return Lista de direcciones del cliente
     */
    public List<Address> findByCustomerId(Integer customerId) {
        String jpql = "SELECT a FROM Address a WHERE a.customer.id = :customerId";
        TypedQuery<Address> query = em.createQuery(jpql, Address.class);
        query.setParameter("customerId", customerId);
        return query.getResultList();
    }

    /**
     * Actualiza una dirección existente
     * @param address Dirección a actualizar
     */
    public void update(Address address) {
        try {
            em.getTransaction().begin();
            em.merge(address);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error al actualizar la dirección: " + e.getMessage());
        }
    }

    /**
     * Elimina una dirección (si no está siendo usada por pedidos)
     * @param addressId ID de la dirección a eliminar
     */
    public void delete(Integer addressId) {
        try {
            em.getTransaction().begin();
            Address address = em.find(Address.class, addressId);
            if (address != null) {
                em.remove(address);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error al eliminar la dirección: " + e.getMessage());
        }
    }

    // para que te saque los datos del cliente enteros para que luego podamos sacar las direcciones que tiene
    public List<Address> findByCustomer(Customer customer) {
        String jpql = "SELECT a FROM Address a WHERE a.customer.id = :customerId";
        TypedQuery<Address> query = em.createQuery(jpql, Address.class);
        query.setParameter("customerId", customer.getId());
        return query.getResultList();
    }

}
