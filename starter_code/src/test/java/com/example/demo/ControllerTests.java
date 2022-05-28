package com.example.demo;

import com.example.demo.controllers.CartController;
import com.example.demo.controllers.ItemController;
import com.example.demo.controllers.OrderController;
import com.example.demo.controllers.UserController;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import com.example.demo.model.requests.ModifyCartRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
public class ControllerTests {

    private UserController userController;
    private final UserRepository userRepository = mock(UserRepository.class);
    private final CartRepository cartRepository = mock(CartRepository.class);
    private final BCryptPasswordEncoder encoder = mock(BCryptPasswordEncoder.class);

    private ItemController itemController;
    private final ItemRepository itemRepository = mock(ItemRepository.class);

    private OrderController orderController;
    private final OrderRepository orderRepository = mock(OrderRepository.class);

    private CartController cartController;

    @Before
    public void setup() {
        userController = new UserController();
        TestUtils.injectObjects(userController, "userRepository", userRepository);
        TestUtils.injectObjects(userController, "cartRepository", cartRepository);
        TestUtils.injectObjects(userController, "bCryptPasswordEncoder", encoder);
        itemController = new ItemController();
        TestUtils.injectObjects(itemController, "itemRepository", itemRepository);
        orderController = new OrderController();
        TestUtils.injectObjects(orderController, "orderRepository", orderRepository);
        TestUtils.injectObjects(orderController, "userRepository", userRepository);
        cartController = new CartController();
        TestUtils.injectObjects(cartController, "userRepository", userRepository);
        TestUtils.injectObjects(cartController, "cartRepository", cartRepository);
        TestUtils.injectObjects(cartController, "itemRepository", itemRepository);
    }

    private User createUserManually(Long id, String username, String hashedPassword) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(hashedPassword);
        return user;
    }

    private ResponseEntity<User> createUserUsingController(String username, String password, String confirmPassword) {
        CreateUserRequest cur = new CreateUserRequest();
        cur.setUsername(username);
        cur.setPassword(password);
        cur.setConfirmPassword(confirmPassword);
        return userController.createUser(cur);
    }

    @Test
    public void testFindUserById() {
        User user = createUserManually(1L, "Lam", "hashedPassword");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        ResponseEntity<User> response = userController.findById(1L);
        Assert.assertNotNull(response);
        Assert.assertEquals(200, response.getStatusCodeValue());
        user = response.getBody();
        Assert.assertEquals("Lam", user.getUsername());
        Assert.assertEquals("hashedPassword", user.getPassword());
    }

    @Test
    public void testFindUserByUsername() {
        User newUser = createUserManually(1L, "Lam", "hashedPassword");
        when(userRepository.findByUsername("Lam")).thenReturn(newUser);
        ResponseEntity<User> response = userController.findByUserName("Lam");
        Assert.assertEquals(200, response.getStatusCodeValue());
        User user = response.getBody();
        Assert.assertEquals("Lam", user.getUsername());
        Assert.assertEquals("hashedPassword", user.getPassword());
    }

    @Test
    public void testCreateUser() throws Exception {
        when(encoder.encode("originalPassword")).thenReturn("hashedPassword");

        // Test success case
        ResponseEntity<User> response = createUserUsingController("LamTran", "originalPassword", "originalPassword");
        Assert.assertNotNull(response);
        Assert.assertEquals(200, response.getStatusCodeValue());
        User user = response.getBody();
        Assert.assertNotNull(user);
        Assert.assertEquals(0, user.getId());
        Assert.assertEquals("LamTran", user.getUsername());
        Assert.assertEquals("hashedPassword", user.getPassword());

        // Test null handler
        response = createUserUsingController(null, "originalPassword", "originalPassword");;
        Assert.assertNotNull(response);
        Assert.assertEquals(400, response.getStatusCodeValue());

        // Test short password handler
        response = createUserUsingController("LamTran", "pass", "pass");
        Assert.assertNotNull(response);
        Assert.assertEquals(400, response.getStatusCodeValue());

        // Test confirm password handler
        response = createUserUsingController("Lam123", "password1", "password2");;
        Assert.assertNotNull(response);
        Assert.assertEquals(400, response.getStatusCodeValue());
    }

    private Item getNewItem(Long id, String name, BigDecimal price, String description) {
        Item item = new Item();
        item.setId(id);
        item.setName(name);
        item.setPrice(price);
        item.setDescription(description);
        return item;
    }

    @Test
    public void testGetItemById() {
        Item newItem = getNewItem(1L, "Book", new BigDecimal(1.0), "For studying magic");
        when(itemRepository.findById(1L)).thenReturn(Optional.of(newItem));
        ResponseEntity<Item> response = itemController.getItemById(1L);
        Assert.assertNotNull(response);
        Assert.assertEquals(200, response.getStatusCodeValue());
        Item item = response.getBody();
        Assert.assertEquals((Long)1L, item.getId());
        Assert.assertEquals("Book", item.getName());
        Assert.assertEquals(new BigDecimal(1.0), item.getPrice());
        Assert.assertEquals("For studying magic", item.getDescription());
        Assert.assertTrue(item.equals(newItem));
    }

    @Test
    public void testGetAllItems() {
        Item newItem = getNewItem(1L, "Keyboard", new BigDecimal(1.0), "Keyboard for gamers");
        when(itemRepository.findAll()).thenReturn(List.of(newItem));
        ResponseEntity<List<Item>> response = itemController.getItems();
        Assert.assertEquals(200, response.getStatusCodeValue());
        List<Item> items = response.getBody();
        Assert.assertEquals(1, items.size());
        Assert.assertEquals(new BigDecimal(1.0), items.get(0).getPrice());
        Assert.assertEquals("Keyboard for gamers", items.get(0).getDescription());
    }

    @Test
    public void testGetItemsByName() {
        Item newItem1 = getNewItem(1L, "Ring", new BigDecimal(1.0), "For wedding");
        Item newItem2 = getNewItem(1L, "Ring", new BigDecimal(1.0), "For ruling Middle Earth");
        when(itemRepository.findByName("Ring")).thenReturn(List.of(newItem1, newItem2));
        ResponseEntity<List<Item>> response = itemController.getItemsByName("Ring");
        Assert.assertEquals(200, response.getStatusCodeValue());
        List<Item> items = response.getBody();
        Assert.assertEquals(2, items.size());
        Assert.assertEquals("For wedding", items.get(0).getDescription());
        Assert.assertEquals("For ruling Middle Earth", items.get(1).getDescription());
    }

    private ResponseEntity<Cart> addToCart(String username, Long itemId, int quantity) {
        ModifyCartRequest mcr = new ModifyCartRequest();
        mcr.setUsername(username);
        mcr.setItemId(itemId);
        mcr.setQuantity(quantity);
        return cartController.addToCart(mcr);
    }

    private ResponseEntity<Cart> removeFromCart(String username, Long itemId, int quantity) {
        ModifyCartRequest mcr = new ModifyCartRequest();
        mcr.setUsername(username);
        mcr.setItemId(itemId);
        mcr.setQuantity(quantity);
        return cartController.removeFromCart(mcr);
    }

    private Cart getNewCart(Long id, User user) {
        Cart cart = new Cart();
        cart.setId(id);
        cart.setUser(user);
        return cart;
    }

    @Test
    public void testAddToCart() {
        User user = createUserManually(1L, "Lam", "strongPassword");
        Item item = getNewItem(1L, "Helmet", new BigDecimal(1.0), "best buy");

        when(userRepository.findByUsername("Lam")).thenReturn(user);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        // Bind cart and user
        Cart cart = getNewCart(1L, user);
        user.setCart(cart);

        // This function calls the method in CartController
        ResponseEntity<Cart> response = addToCart("Lam", 1L, 1);

        // Testing some return values
        Assert.assertNotNull(response);
        Assert.assertEquals(200, response.getStatusCodeValue());
        cart = response.getBody();
        Assert.assertEquals((Long)1L, cart.getId());
        Assert.assertEquals("Lam", cart.getUser().getUsername());
        Assert.assertEquals(1, cart.getItems().size());
        Assert.assertEquals("Helmet", cart.getItems().get(0).getName());

        // call addToCart once more to test cart size
        addToCart("Lam", 1L, 2);
        Assert.assertEquals(3, cart.getItems().size());

        // Test when user is not found
        response = addToCart("Not Lam", 1L, 1);
        Assert.assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    public void testRemoveFromCart() {
        User user = createUserManually(1L, "Lam", "strongPassword");
        Item item = getNewItem(1L, "Helmet", new BigDecimal(1.0), "best buy");

        when(userRepository.findByUsername("Lam")).thenReturn(user);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        // Bind cart and user
        Cart cart = getNewCart(1L, user);
        user.setCart(cart);

        cart = addToCart("Lam", 1L, 5).getBody();
        // Just checking item count
        Assert.assertEquals(5, cart.getItems().size());

        ResponseEntity<Cart> response = removeFromCart("Lam", 1L, 2);
        Assert.assertEquals(200, response.getStatusCodeValue());
        cart = response.getBody();
        // Checking if the correct cart object is returned
        Assert.assertEquals("Lam", cart.getUser().getUsername());
        // Checking if final quantity is 5 - 2 = 3
        Assert.assertEquals(3, cart.getItems().size());
    }

    @Test
    public void testSubmitOrder() {
        User user = createUserManually(1L, "Lam", "strongPassword");
        Item item = getNewItem(1L, "Helmet", new BigDecimal(1.0), "best buy");

        when(userRepository.findByUsername("Lam")).thenReturn(user);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        // Bind cart and user
        Cart cart = getNewCart(1L, user);
        user.setCart(cart);
        addToCart("Lam", 1L, 1);

        when(userRepository.findByUsername("Lam")).thenReturn(user);
        ResponseEntity<UserOrder> response = orderController.submit("Lam");
        Assert.assertEquals(200, response.getStatusCodeValue());
        UserOrder order = response.getBody();
        Assert.assertEquals(cart.getUser(), order.getUser());
        Assert.assertEquals(1, order.getItems().size());
        Assert.assertEquals("Helmet", order.getItems().get(0).getName());
        Assert.assertEquals(cart.getTotal(), order.getTotal());

        // Test when user is not found
        response = orderController.submit("Not Lam");
        Assert.assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    public void testGetOrdersForUser() {
        // Create order with and user and 2 items
        User user = createUserManually(1L, "Lam", "strongPassword");
        UserOrder order = new UserOrder();
        Item item1 = getNewItem(1L, "Helmet", new BigDecimal(1.0), "best buy");
        Item item2 = getNewItem(1L, "Monitor", new BigDecimal(1.0), "best buy");
        order.setUser(user);
        order.setItems(List.of(item1, item2));
        // Mocking repositories' return values
        when(userRepository.findByUsername("Lam")).thenReturn(user);
        when(orderRepository.findByUser(user)).thenReturn(List.of(order));
        // Testing if the function call the repositories as expected
        ResponseEntity<List<UserOrder>> response = orderController.getOrdersForUser("Lam");
        Assert.assertEquals(200, response.getStatusCodeValue());
        List<UserOrder> orders = response.getBody();
        Assert.assertEquals(1, orders.size());
        Assert.assertEquals(2, orders.get(0).getItems().size());
        Assert.assertEquals("Monitor", orders.get(0).getItems().get(1).getName());

        // Test when user is not found
        response = orderController.getOrdersForUser("Not Lam");
        Assert.assertEquals(404, response.getStatusCodeValue());
    }

}