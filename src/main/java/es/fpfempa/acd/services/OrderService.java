package es.fpfempa.acd.services;

import es.fpfempa.acd.dao.AddressDao;
import es.fpfempa.acd.dao.OrderDao;
import es.fpfempa.acd.entities.Address;
import es.fpfempa.acd.entities.Customer;
import es.fpfempa.acd.entities.Order;

import jakarta.persistence.EntityManager;
import java.util.List;

public class OrderService {
    private OrderDao orderDao;
    private AddressDao addressDao;
    private EntityManager em;

    public OrderService(EntityManager em) {
        this.em = em;
        this.orderDao = new OrderDao(em);
        this.addressDao = new AddressDao(em);
    }

    /**
     * Obtiene los detalles completos de un pedido (con customer y address cargados)
     */
    public Order getOrderDetails(Integer orderId) {
        return orderDao.findByIdConDetalles(orderId);
    }

    /**
     * Obtiene todas las direcciones existentes de un cliente
     */
    public List<Address> getCustomerAddresses(Customer customer) {
        return addressDao.findByCustomer(customer);
    }

    /**
     * Crea una nueva dirección para un cliente
     */
    public Address createNewAddress(Customer customer, String line1, String city, String country) {
        Address nuevaAddress = new Address();
        nuevaAddress.setLine1(line1);
        nuevaAddress.setCity(city);
        nuevaAddress.setCountry(country);
        nuevaAddress.setCustomer(customer);

        addressDao.create(nuevaAddress);
        return nuevaAddress;
    }

    /**
     * Cambia la dirección de envío de un pedido y lo actualiza
     */
    public void changeShippingAddress(Order order, Address newAddress) {
        em.getTransaction().begin();
        try {
            order.setShippingAddress(newAddress);
            orderDao.update(order);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Error al cambiar la dirección del pedido", e);
        }
    }

    /**
     * Verifica que el pedido existe
     */
    public boolean orderExists(Integer orderId) {
        Order order = orderDao.findById(orderId);
        return order != null;
    }
}
