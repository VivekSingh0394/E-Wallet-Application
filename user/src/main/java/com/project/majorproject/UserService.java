package com.project.majorproject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

@Service
public class UserService {
    @Autowired
    RedisTemplate<String,User> redisTemplate;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    UserRepository userRepository;
    @Autowired
    KafkaTemplate<String,String> kafkaTemplate;// for sending message to create wallet when user is created


    public User findByUserName(String userName)
    {
        // first find in cache if found return
        // if not found ,find in Db,and store in cache and return

        // finding from cache
         Map map = redisTemplate.opsForHash().entries(userName);
         User user = null;
         // if not found i.e map is  null
         if(map == null)
         {
             // find from Db
             user = userRepository.findByuserName(userName);
              // save in cache
             saveInCache(user);

             return user;
         }
         else // means its in cache
         {
             // convert map to user since it is stored in string----map combination so object mapper will do that conversion
              user = objectMapper.convertValue(map,User.class); // converted from map to userclass
             return user;
         }

    }
    public String addUser(UserRequest userRequest)
    {
        User user = User.builder().userName(userRequest.getUserName()).age(userRequest.getAge())
                .mobile(userRequest.getMobile()).email(userRequest.getEmail()).name(userRequest.getName()).build();
        // save in DB
        userRepository.save(user);
        // save in cache
        saveInCache(user);
        // now we need to send a message to wallet module so as to create a wallet when a user is added
        kafkaTemplate.send("create_wallet",user.getUserName());

        return "User Added Succesfully";
    }
    public void saveInCache(User user)
    {
        // cache expects string --- map combunation
        // converting user to mapclass
        Map map =objectMapper.convertValue(user,Map.class);
        // modified key for prevention of collision of keys in hashmap so converting key to modified key using PREFIX
        String key = "USER_KEY"+user.getUserName();
        redisTemplate.opsForHash().putAll(key,map);
        // set expiry of 12 hours since cache has small memeory so we need to release for others also
        redisTemplate.expire(user.getUserName(), Duration.ofHours(12));
    }

    public UserResponseDto findEmailAndName(String userName)
    {
        User user = userRepository.findByuserName(userName);
        UserResponseDto userResponseDto = UserResponseDto.builder().email(user.getEmail()).name(user.getName()).build();
        return userResponseDto;
    }
}
