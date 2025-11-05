# Gestión de Direcciones de Pedidos - Guía de Implementación

## 📋 Descripción del Ejercicio

Desarrollar un programa que permita consultar y modificar la dirección de envío de un pedido. El flujo será:
1. Solicitar el ID del pedido
2. Mostrar información del pedido
3. Preguntar si desea cambiar la dirección de envío
4. Si acepta, elegir entre crear una nueva dirección o usar una existente
5. Actualizar el pedido con la nueva dirección de envío

---

## 🎯 Conceptos Clave que Debes Entender

### ¿Qué son las Entidades JPA?

**Las entidades JPA son clases Java normales que representan tablas de la base de datos.** 

Piensa en ellas como "traductoras":
- La base de datos tiene una tabla `customers` con columnas → JPA la convierte en una clase `Customer` con atributos
- Cuando haces un `SELECT * FROM customers` → JPA lo convierte en objetos `Customer` de Java
- Cuando creas un nuevo objeto `Customer` y lo guardas → JPA lo inserta en la tabla `customers`

**NO necesitas carpeta `models` separada.** Las entidades que tienes en `entities/` ya son tus modelos. Ya tienen:
- ✅ Constructores (puedes añadir más si necesitas)
- ✅ Getters y Setters
- ✅ Anotaciones que mapean con la base de datos (`@Entity`, `@Table`, `@Column`)

### ¿Para qué sirven los DAO (Data Access Object)?

Los DAO son clases que **contienen los métodos para acceder a la base de datos**. Separan la lógica de acceso a datos del resto de tu programa.

Ejemplo: 
- Entidad `Customer` → Es el objeto que representa un cliente
- `CustomerDAO` → Tiene métodos como `buscarPorId()`, `guardar()`, `eliminar()`, etc.

**Sí necesitas DAOs** porque:
- Las entidades JPA solo son objetos de datos
- Los DAOs contienen la lógica para hacer consultas, guardar, actualizar, etc.
- Hacen tu código más organizado y reutilizable

---

## 🏗️ Estructura del Proyecto

```
javaFromDb/
├── src/main/java/es/fpfempa/acd/
│   ├── Main.java                    ← AQUÍ VA TU PROGRAMA PRINCIPAL
│   ├── entities/                    ← CLASES QUE REPRESENTAN TABLAS
│   │   ├── Address.java            (Ya creada ✓)
│   │   ├── Customer.java           (Ya creada ✓)
│   │   └── Order.java              (Ya creada ✓)
│   ├── dao/                         ← CLASES PARA ACCEDER A LA BD
│   │   ├── AddressDAO.java         (Vacía - HAY QUE IMPLEMENTAR)
│   │   ├── OrderDao.java           (Vacía - HAY QUE IMPLEMENTAR)
│   │   └── CustomerDAO.java        (No existe - HAY QUE CREAR)
│   └── services/                    ← LÓGICA DE NEGOCIO (opcional)
│       └── OrderService.java       (PUEDES CREAR ESTO PARA ORGANIZAR)
└── src/main/resources/
    └── META-INF/
        └── persistence.xml          (Ya configurado ✓)
```

---

## 📝 Plan de Implementación Paso a Paso

### **FASE 1: Preparar los DAOs (Acceso a Datos)**

#### 1.1. Implementar `OrderDao.java`

**¿Qué debe hacer?**
- Buscar un pedido por su ID
- Actualizar un pedido (para cambiar la dirección de envío)
- Mostrar información completa del pedido

**Métodos que necesitas crear:**
```
- findById(Integer orderId) : Order
- update(Order order) : void
- findByIdWithDetails(Integer orderId) : Order (carga Customer y Address)
```

**Pistas:**
- Usa `EntityManager` para hacer las consultas
- Para buscar: `entityManager.find(Order.class, id)`
- Para actualizar: `entityManager.merge(order)`
- Recuerda abrir y cerrar transacciones con `entityManager.getTransaction().begin()` y `.commit()`

---

#### 1.2. Implementar `AddressDAO.java`

**¿Qué debe hacer?**
- Buscar todas las direcciones de un cliente
- Crear una nueva dirección
- Buscar una dirección por ID

**Métodos que necesitas crear:**
```
- findByCustomer(Customer customer) : List<Address>
- save(Address address) : void
- findById(Integer addressId) : Address
```

**Pistas:**
- Para listar direcciones de un cliente usa JPQL:
  ```
  "SELECT a FROM Address a WHERE a.customer.id = :customerId"
  ```
- Para guardar una nueva dirección: `entityManager.persist(address)`

---

#### 1.3. (OPCIONAL) Crear `CustomerDAO.java`

Si necesitas buscar clientes o hacer operaciones adicionales.

**Métodos que podrías necesitar:**
```
- findById(Integer customerId) : Customer
```

---

### **FASE 2: Crear la Lógica de Negocio (Opcional pero Recomendado)**

#### 2.1. Crear `OrderService.java` en la carpeta `services/`

**¿Para qué sirve?**
Coordinar las operaciones entre varios DAOs. Separa la lógica de negocio de la interfaz de usuario (Main).

**Métodos que deberías crear:**
```
- getOrderDetails(Integer orderId) : Order
- changeShippingAddress(Order order, Address newAddress) : void
- createNewAddressForCustomer(Customer customer, datos de la dirección) : Address
- getCustomerAddresses(Customer customer) : List<Address>
```

**Ventaja:** Tu Main solo llama a estos métodos en lugar de llamar directamente a los DAOs.

---

### **FASE 3: Implementar el Main (Interfaz de Usuario)**

#### 3.1. Estructura del `Main.java`

```java
public class Main {
    public static void main(String[] args) {
        // 1. Crear EntityManagerFactory
        // 2. Crear DAOs (pasándoles el EntityManager)
        // 3. Crear Scanner para leer entrada del usuario
        // 4. Implementar el flujo del programa
        // 5. Cerrar EntityManager y EntityManagerFactory
    }
}
```

#### 3.2. Flujo del Programa (paso a paso)

**PASO 1: Inicialización**
- Crear `EntityManagerFactory` usando `Persistence.createEntityManagerFactory("acdPU")`
- Crear `EntityManager` usando `emf.createEntityManager()`
- Instanciar los DAOs pasándoles el EntityManager
- Crear un `Scanner` para leer del teclado

**PASO 2: Pedir ID del Pedido**
```
→ Mostrar: "Introduce el ID del pedido:"
→ Leer el ID con Scanner
→ Buscar el pedido con OrderDao.findByIdWithDetails(id)
→ Si no existe, mostrar error y terminar
```

**PASO 3: Mostrar Información del Pedido**
```
→ Mostrar: ID del pedido, fecha, estado, monto total
→ Mostrar: Información del cliente (nombre, email)
→ Mostrar: Dirección de envío actual (line1, city, country)
```

**PASO 4: Preguntar si Quiere Cambiar Dirección**
```
→ Mostrar: "¿Deseas cambiar la dirección de envío? (S/N)"
→ Leer respuesta
→ Si responde "N", terminar programa
→ Si responde "S", continuar
```

**PASO 5: Elegir Tipo de Cambio**
```
→ Mostrar: "¿Deseas usar una dirección existente o crear una nueva?"
→ Mostrar: "1. Crear nueva dirección"
→ Mostrar: "2. Usar dirección existente"
→ Leer opción
```

**PASO 6a: Si Elige Crear Nueva Dirección**
```
→ Pedir: line1 (calle)
→ Pedir: city (ciudad)
→ Pedir: country (país - código de 2 letras)
→ Crear objeto Address con los datos
→ Asignar el Customer del pedido a la nueva dirección
→ Guardar la dirección con AddressDAO.save(address)
→ Actualizar el pedido: order.setShippingAddress(nuevaDireccion)
→ Guardar el pedido con OrderDao.update(order)
→ Mostrar: "Dirección actualizada correctamente"
```

**PASO 6b: Si Elige Usar Dirección Existente**
```
→ Obtener el Customer del pedido: order.getCustomer()
→ Buscar direcciones del cliente: AddressDAO.findByCustomer(customer)
→ Si no tiene direcciones, mostrar mensaje y volver a PASO 5
→ Mostrar lista numerada de direcciones:
   1. [line1, city, country]
   2. [line1, city, country]
   ...
→ Pedir: "Elige el número de la dirección:"
→ Leer número
→ Validar que el número está en el rango
→ Obtener la dirección elegida
→ Actualizar el pedido: order.setShippingAddress(direccionElegida)
→ Guardar el pedido con OrderDao.update(order)
→ Mostrar: "Dirección actualizada correctamente"
```

**PASO 7: Verificar Cambio (opcional pero recomendado)**
```
→ Buscar el pedido de nuevo para confirmar el cambio
→ Mostrar la nueva dirección de envío
```

**PASO 8: Cerrar Recursos**
```
→ Cerrar EntityManager
→ Cerrar EntityManagerFactory
→ Cerrar Scanner
```

---

## 🔧 Detalles Técnicos Importantes

### Manejo de EntityManager en DAOs

Puedes hacerlo de dos formas:

**Opción A: Pasar EntityManager en el constructor**
```java
public class OrderDao {
    private EntityManager em;
    
    public OrderDao(EntityManager em) {
        this.em = em;
    }
    
    // métodos que usan this.em
}
```

**Opción B: Crear EntityManager dentro de cada método**
```java
public Order findById(Integer id) {
    EntityManager em = JPAUtil.getEntityManager();
    Order order = em.find(Order.class, id);
    em.close();
    return order;
}
```

💡 **Recomendación:** Usa la Opción A para este ejercicio (más simple).

---

### Gestión de Transacciones

Para **consultas (SELECT)**: No necesitas transacción
```java
Order order = em.find(Order.class, id);
```

Para **modificaciones (INSERT, UPDATE, DELETE)**: Sí necesitas transacción
```java
em.getTransaction().begin();
em.persist(newAddress);  // o em.merge(order)
em.getTransaction().commit();
```

---

### Cargar Relaciones (FetchType.LAZY)

Tus entidades tienen `FetchType.LAZY`, lo que significa que no carga automáticamente las relaciones.

**Problema:**
```java
Order order = em.find(Order.class, id);
em.close();
Customer customer = order.getCustomer(); // ❌ Error: sesión cerrada
```

**Solución 1:** Acceder a las relaciones antes de cerrar el EntityManager
```java
Order order = em.find(Order.class, id);
String nombre = order.getCustomer().getFullName(); // Fuerza la carga
Address direccion = order.getShippingAddress();
String ciudad = direccion.getCity(); // Fuerza la carga
em.close();
```

**Solución 2:** Usar JPQL con JOIN FETCH
```java
String jpql = "SELECT o FROM Order o " +
              "JOIN FETCH o.customer " +
              "LEFT JOIN FETCH o.shippingAddress " +
              "WHERE o.id = :orderId";
Order order = em.createQuery(jpql, Order.class)
                .setParameter("orderId", id)
                .getSingleResult();
```

---

## ✅ Lista de Verificación (Checklist)

Antes de empezar a codificar, asegúrate de entender:

- [ ] ¿Qué son las entidades JPA? → Clases que representan tablas
- [ ] ¿Para qué sirven los DAOs? → Métodos para acceder a la base de datos
- [ ] ¿Necesito carpeta models? → NO, las entities son tus modelos
- [ ] ¿Cómo se crea un EntityManager? → Persistence.createEntityManagerFactory()
- [ ] ¿Cuándo necesito transacciones? → Para INSERT, UPDATE, DELETE
- [ ] ¿Qué es LAZY loading? → Las relaciones no se cargan automáticamente

---

## 📚 Orden de Implementación Recomendado

```
1. OrderDao (método findById básico)
2. Probar en Main que puedes buscar un pedido
3. OrderDao (método findByIdWithDetails con JOIN FETCH)
4. Probar en Main que puedes ver customer y address
5. AddressDAO (método findByCustomer)
6. Probar en Main que puedes listar direcciones
7. AddressDAO (método save)
8. Probar en Main que puedes crear direcciones
9. OrderDao (método update)
10. Probar en Main que puedes actualizar el pedido
11. Implementar el flujo completo en Main
12. (OPCIONAL) Mover lógica a OrderService
13. Probar el programa completo
```

---

## 🐛 Problemas Comunes y Soluciones

### "LazyInitializationException"
**Causa:** Intentas acceder a una relación después de cerrar el EntityManager  
**Solución:** Carga las relaciones antes de cerrar (ver sección "Cargar Relaciones")

### "No transaction is in progress"
**Causa:** Intentas hacer persist/merge sin transacción  
**Solución:** Envuelve en `em.getTransaction().begin()` y `.commit()`

### "detached entity passed to persist"
**Causa:** Intentas hacer persist() de una entidad que ya existe  
**Solución:** Usa `merge()` en lugar de `persist()` para actualizar

### El pedido no se actualiza
**Causa:** No haces commit de la transacción  
**Solución:** Asegúrate de llamar a `em.getTransaction().commit()`

---

## 💡 Consejos Finales

1. **Trabaja en pequeños pasos:** Implementa un método, pruébalo, luego sigue
2. **Usa System.out.println() para depurar:** Imprime los objetos para ver qué contienen
3. **Maneja excepciones:** Usa try-catch para capturar errores
4. **Valida la entrada del usuario:** Comprueba que los IDs existen antes de usarlos
5. **Cierra recursos siempre:** Usa try-finally o try-with-resources
6. **Lee los mensajes de error:** Hibernate te da pistas sobre qué falla

---

## 🎓 Preguntas para Auto-Evaluación

Después de completar el ejercicio, deberías poder responder:

1. ¿Qué diferencia hay entre `persist()` y `merge()`?
2. ¿Por qué necesito transacciones?
3. ¿Qué hace `FetchType.LAZY`?
4. ¿Cómo cargo una relación LAZY explícitamente?
5. ¿Para qué sirve el archivo `persistence.xml`?
6. ¿Cuál es la diferencia entre Entity y DAO?
7. ¿Cómo se relacionan Order, Customer y Address en la base de datos?

---

## 📖 Recursos Adicionales

- Documentación JPA: https://jakarta.ee/specifications/persistence/3.0/
- Hibernate Guide: https://hibernate.org/orm/documentation/
- JPQL Tutorial: https://www.objectdb.com/java/jpa/query/jpql/structure

---

**¡Mucha suerte con tu ejercicio! Recuerda: las entidades que tienes ya son suficientes, no necesitas modelos adicionales. Solo implementa los DAOs y el Main siguiendo este plan.** 🚀

