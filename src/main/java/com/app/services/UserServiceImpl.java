package com.app.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.app.config.AppConstants;
import com.app.entites.Address;
import com.app.entites.Cart;
import com.app.entites.CartItem;
import com.app.entites.Role;
import com.app.entites.User;
import com.app.exceptions.APIException;
import com.app.exceptions.ResourceNotFoundException;
import com.app.payloads.AddressDTO;
import com.app.payloads.CartDTO;
import com.app.payloads.ProductDTO;
import com.app.payloads.UserDTO;
import com.app.payloads.UserResponse;
import com.app.repositories.AddressRepo;
import com.app.repositories.RoleRepo;
import com.app.repositories.UserRepo;
import com.app.repositories.CartRepo;

import jakarta.transaction.Transactional;

@Transactional
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RoleRepo roleRepo;
    @Autowired
    private CartRepo cartRepo;

    @Autowired
    private AddressRepo addressRepo;

    @Autowired
    private CartService cartService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public UserDTO registerUser(UserDTO userDTO) {
        try {
            User user = modelMapper.map(userDTO, User.class);

            Cart cart = new Cart();
            user.setCart(cart);
            
        

            Role role = roleRepo.findById(AppConstants.USER_ID)
                    .orElseThrow(() -> new APIException("Default role not found!"));
            user.getRoles().add(role);

            AddressDTO addrDTO = userDTO.getAddress();
            Address address = addressRepo.findByCountryAndStateAndCityAndPincodeAndStreetAndBuildingName(
                    addrDTO.getCountry(), addrDTO.getState(), addrDTO.getCity(), addrDTO.getPincode(),
                    addrDTO.getStreet(), addrDTO.getBuildingName());

            if (address == null) {
                address = new Address(addrDTO.getCountry(), addrDTO.getState(), addrDTO.getCity(),
                        addrDTO.getPincode(), addrDTO.getStreet(), addrDTO.getBuildingName());
                address = addressRepo.save(address);
            }

            user.setAddresses(List.of(address));
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

            User registeredUser = userRepo.save(user);
            cart.setUser(registeredUser);

            UserDTO responseDTO = modelMapper.map(registeredUser, UserDTO.class);

            registeredUser.getAddresses().stream().findFirst()
                    .ifPresent(addr -> responseDTO.setAddress(modelMapper.map(addr, AddressDTO.class)));

            return responseDTO;
        } catch (DataIntegrityViolationException e) {
            throw new APIException("User already exists with emailId: " + userDTO.getEmail());
        }
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);
        user.getAddresses().stream().findFirst()
                .ifPresent(addr -> userDTO.setAddress(modelMapper.map(addr, AddressDTO.class)));
        return userDTO;
    }

    @Override
    public UserResponse getAllUsers(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<User> userPage = userRepo.findAll(pageable);
        List<UserDTO> userDTOs = userPage.getContent().stream().map(user -> {
            UserDTO dto = modelMapper.map(user, UserDTO.class);
            user.getAddresses().stream().findFirst()
                    .ifPresent(addr -> dto.setAddress(modelMapper.map(addr, AddressDTO.class)));
            CartDTO cartDTO = modelMapper.map(user.getCart(), CartDTO.class);
            List<ProductDTO> products = user.getCart().getCartItems().stream()
                    .map(item -> modelMapper.map(item.getProduct(), ProductDTO.class)).collect(Collectors.toList());
            cartDTO.setProducts(products);
            dto.setCart(cartDTO);
            return dto;
        }).collect(Collectors.toList());

        if (userDTOs.isEmpty()) {
            throw new APIException("No User exists !!!");
        }

        UserResponse response = new UserResponse();
        response.setContent(userDTOs);
        response.setPageNumber(userPage.getNumber());
        response.setPageSize(userPage.getSize());
        response.setTotalElements(userPage.getTotalElements());
        response.setTotalPages(userPage.getTotalPages());
        response.setLastPage(userPage.isLast());

        return response;
    }

    @Override
    public UserDTO getUserById(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        user.getAddresses().stream().findFirst()
                .ifPresent(addr -> userDTO.setAddress(modelMapper.map(addr, AddressDTO.class)));

        CartDTO cartDTO = modelMapper.map(user.getCart(), CartDTO.class);
        List<ProductDTO> products = user.getCart().getCartItems().stream()
                .map(item -> modelMapper.map(item.getProduct(), ProductDTO.class)).collect(Collectors.toList());
        cartDTO.setProducts(products);
        userDTO.setCart(cartDTO);

        return userDTO;
    }

    @Override
    public UserDTO updateUser(Long userId, UserDTO userDTO) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setMobileNumber(userDTO.getMobileNumber());
        user.setEmail(userDTO.getEmail());

        if (userDTO.getPassword() != null && !userDTO.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        if (userDTO.getAddress() != null) {
            AddressDTO addrDTO = userDTO.getAddress();
            Address address = addressRepo.findByCountryAndStateAndCityAndPincodeAndStreetAndBuildingName(
                    addrDTO.getCountry(), addrDTO.getState(), addrDTO.getCity(), addrDTO.getPincode(),
                    addrDTO.getStreet(), addrDTO.getBuildingName());

            if (address == null) {
                address = new Address(addrDTO.getCountry(), addrDTO.getState(), addrDTO.getCity(),
                        addrDTO.getPincode(), addrDTO.getStreet(), addrDTO.getBuildingName());
                address = addressRepo.save(address);
            }

            user.setAddresses(List.of(address));
        }

        User updatedUser = userRepo.save(user);
        UserDTO updatedDTO = modelMapper.map(updatedUser, UserDTO.class);

        updatedUser.getAddresses().stream().findFirst()
                .ifPresent(addr -> updatedDTO.setAddress(modelMapper.map(addr, AddressDTO.class)));

        CartDTO cartDTO = modelMapper.map(user.getCart(), CartDTO.class);
        List<ProductDTO> products = user.getCart().getCartItems().stream()
                .map(item -> modelMapper.map(item.getProduct(), ProductDTO.class)).collect(Collectors.toList());
        cartDTO.setProducts(products);
        updatedDTO.setCart(cartDTO);

        return updatedDTO;
    }
@Override
public String deleteUser(Long userId) {
    User user = userRepo.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

    String email = user.getEmail();

    user.getCart().getCartItems().forEach(item -> {
        Long productId = item.getProduct().getProductId();
        cartService.deleteProductFromUserCart(email, productId);
    });

    userRepo.delete(user);
    return "User with userId " + userId + " deleted successfully!!!";
}
} 
