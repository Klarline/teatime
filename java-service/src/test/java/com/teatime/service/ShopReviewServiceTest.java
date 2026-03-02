package com.teatime.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.teatime.dto.Result;
import com.teatime.entity.Shop;
import com.teatime.entity.ShopReview;
import com.teatime.entity.User;
import com.teatime.repository.ShopReviewRepository;
import com.teatime.repository.UserRepository;
import com.teatime.service.impl.ShopReviewServiceImpl;
import com.teatime.utils.UserHolder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ShopReviewServiceTest {

  @Mock
  private ShopReviewRepository shopReviewRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private IShopService shopService;

  @Mock
  private IAIService aiService;

  private ShopReviewServiceImpl shopReviewService;

  @BeforeEach
  void setUp() {
    shopReviewService = new ShopReviewServiceImpl();
    ReflectionTestUtils.setField(shopReviewService, "shopReviewRepository", shopReviewRepository);
    ReflectionTestUtils.setField(shopReviewService, "userRepository", userRepository);
    ReflectionTestUtils.setField(shopReviewService, "shopService", shopService);
    ReflectionTestUtils.setField(shopReviewService, "aiService", aiService);
  }

  @Test
  void testCreateReview_AlreadyReviewed_ReturnsFail() {
    ShopReview review = new ShopReview();
    review.setShopId(1L);
    review.setRating(5);

    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class)) {
      userHolderMock.when(UserHolder::getUser).thenReturn(createUserDTO(1L));
      when(shopReviewRepository.existsByUserIdAndShopId(1L, 1L)).thenReturn(true);

      Result result = shopReviewService.createReview(review);

      assertFalse(result.getSuccess());
      assertEquals("You have already reviewed this shop", result.getErrorMsg());
      verify(shopReviewRepository, never()).save(any());
    }
  }

  @Test
  void testCreateReview_InvalidRating_ReturnsFail() {
    ShopReview review = new ShopReview();
    review.setShopId(1L);
    review.setRating(0);

    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class)) {
      userHolderMock.when(UserHolder::getUser).thenReturn(createUserDTO(1L));
      when(shopReviewRepository.existsByUserIdAndShopId(1L, 1L)).thenReturn(false);

      Result result = shopReviewService.createReview(review);

      assertFalse(result.getSuccess());
      assertEquals("Rating must be between 1 and 5", result.getErrorMsg());
    }
  }

  @Test
  void testCreateReview_RatingAbove5_ReturnsFail() {
    ShopReview review = new ShopReview();
    review.setShopId(1L);
    review.setRating(6);

    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class)) {
      userHolderMock.when(UserHolder::getUser).thenReturn(createUserDTO(1L));
      when(shopReviewRepository.existsByUserIdAndShopId(1L, 1L)).thenReturn(false);

      Result result = shopReviewService.createReview(review);

      assertFalse(result.getSuccess());
      assertEquals("Rating must be between 1 and 5", result.getErrorMsg());
    }
  }

  @Test
  void testCreateReview_SaveReturnsNull_ReturnsFail() {
    ShopReview review = new ShopReview();
    review.setShopId(1L);
    review.setRating(5);

    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class)) {
      userHolderMock.when(UserHolder::getUser).thenReturn(createUserDTO(1L));
      when(shopReviewRepository.existsByUserIdAndShopId(1L, 1L)).thenReturn(false);
      when(shopReviewRepository.save(any(ShopReview.class))).thenReturn(null);

      Result result = shopReviewService.createReview(review);

      assertFalse(result.getSuccess());
      assertEquals("Failed to create review", result.getErrorMsg());
    }
  }

  @Test
  void testGetShopReviews_UserNotFound_ReturnsReviewWithoutNickName() {
    ShopReview review = new ShopReview();
    review.setId(1L);
    review.setUserId(99L);
    review.setContent("Good");
    when(shopReviewRepository.findByShopIdOrderByCreateTimeDesc(eq(1L), any(PageRequest.class)))
        .thenReturn(new PageImpl<>(List.of(review)));
    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    Result result = shopReviewService.getShopReviews(1L, 1, 10);

    assertTrue(result.getSuccess());
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> reviews = (List<Map<String, Object>>) result.getData();
    assertEquals(1, reviews.size());
    assertFalse(reviews.get(0).containsKey("nickName"));
  }

  @Test
  void testGetShopRatingStats_AvgRatingNotNumber_DefaultsToZero() {
    when(shopReviewRepository.findAvgRatingAndCountByShopId(1L))
        .thenReturn(new Object[]{"invalid", 5L});

    Result result = shopReviewService.getShopRatingStats(1L);

    assertTrue(result.getSuccess());
    @SuppressWarnings("unchecked")
    Map<String, Object> stats = (Map<String, Object>) result.getData();
    assertEquals(0.0, stats.get("avgRating"));
    assertEquals(5L, stats.get("reviewCount"));
  }

  @Test
  void testCreateReview_Success() {
    ShopReview review = new ShopReview();
    review.setShopId(1L);
    review.setRating(5);
    review.setContent("Great tea!");

    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class)) {
      userHolderMock.when(UserHolder::getUser).thenReturn(createUserDTO(1L));
      when(shopReviewRepository.existsByUserIdAndShopId(1L, 1L)).thenReturn(false);
      when(shopReviewRepository.save(any(ShopReview.class))).thenAnswer(inv -> {
        ShopReview r = inv.getArgument(0);
        r.setId(1L);
        return r;
      });
      when(shopReviewRepository.findAvgRatingAndCountByShopId(1L))
          .thenReturn(new Object[]{5.0, 1L});
      when(shopService.getById(1L)).thenReturn(createShop(1L, "Test Shop"));
      when(userRepository.findById(1L)).thenReturn(Optional.of(createUser(1L, "User1")));

      Result result = shopReviewService.createReview(review);

      assertTrue(result.getSuccess());
      assertEquals(1L, result.getData());
      verify(shopReviewRepository).save(any(ShopReview.class));
    }
  }

  @Test
  void testGetShopReviews_ReturnsEnrichedReviews() {
    ShopReview review = new ShopReview();
    review.setId(1L);
    review.setUserId(1L);
    review.setContent("Good");
    when(shopReviewRepository.findByShopIdOrderByCreateTimeDesc(eq(1L), any(PageRequest.class)))
        .thenReturn(new PageImpl<>(List.of(review)));
    when(userRepository.findById(1L)).thenReturn(Optional.of(createUser(1L, "Reviewer")));

    Result result = shopReviewService.getShopReviews(1L, 1, 10);

    assertTrue(result.getSuccess());
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> reviews = (List<Map<String, Object>>) result.getData();
    assertEquals(1, reviews.size());
    assertEquals("Reviewer", reviews.get(0).get("nickName"));
  }

  @Test
  void testGetShopRatingStats_ReturnsStats() {
    when(shopReviewRepository.findAvgRatingAndCountByShopId(1L))
        .thenReturn(new Object[]{4.5, 10L});

    Result result = shopReviewService.getShopRatingStats(1L);

    assertTrue(result.getSuccess());
    @SuppressWarnings("unchecked")
    Map<String, Object> stats = (Map<String, Object>) result.getData();
    assertEquals(4.5, stats.get("avgRating"));
    assertEquals(10L, stats.get("reviewCount"));
  }

  @Test
  void testGetShopRatingStats_NullAvgRating_HandlesGracefully() {
    when(shopReviewRepository.findAvgRatingAndCountByShopId(1L))
        .thenReturn(new Object[]{null, 0L});

    Result result = shopReviewService.getShopRatingStats(1L);

    assertTrue(result.getSuccess());
    @SuppressWarnings("unchecked")
    Map<String, Object> stats = (Map<String, Object>) result.getData();
    assertEquals(0.0, stats.get("avgRating"));
    assertEquals(0L, stats.get("reviewCount"));
  }

  @Test
  void testHasUserReviewed_True() {
    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class)) {
      userHolderMock.when(UserHolder::getUser).thenReturn(createUserDTO(1L));
      when(shopReviewRepository.existsByUserIdAndShopId(1L, 1L)).thenReturn(true);

      Result result = shopReviewService.hasUserReviewed(1L);

      assertTrue(result.getSuccess());
      assertTrue((Boolean) result.getData());
    }
  }

  @Test
  void testHasUserReviewed_False() {
    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class)) {
      userHolderMock.when(UserHolder::getUser).thenReturn(createUserDTO(1L));
      when(shopReviewRepository.existsByUserIdAndShopId(1L, 1L)).thenReturn(false);

      Result result = shopReviewService.hasUserReviewed(1L);

      assertTrue(result.getSuccess());
      assertFalse((Boolean) result.getData());
    }
  }

  @Test
  void testDeleteReview_NotFound_ReturnsFail() {
    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class)) {
      userHolderMock.when(UserHolder::getUser).thenReturn(createUserDTO(1L));
      when(shopReviewRepository.findById(999L)).thenReturn(Optional.empty());

      Result result = shopReviewService.deleteReview(999L);

      assertFalse(result.getSuccess());
      assertEquals("Review not found", result.getErrorMsg());
    }
  }

  @Test
  void testDeleteReview_NotOwnReview_ReturnsFail() {
    ShopReview review = new ShopReview();
    review.setId(1L);
    review.setUserId(2L);
    review.setShopId(1L);

    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class)) {
      userHolderMock.when(UserHolder::getUser).thenReturn(createUserDTO(1L));
      when(shopReviewRepository.findById(1L)).thenReturn(Optional.of(review));

      Result result = shopReviewService.deleteReview(1L);

      assertFalse(result.getSuccess());
      assertEquals("You can only delete your own reviews", result.getErrorMsg());
      verify(shopReviewRepository, never()).deleteById(any());
    }
  }

  @Test
  void testDeleteReview_Success() {
    ShopReview review = new ShopReview();
    review.setId(1L);
    review.setUserId(1L);
    review.setShopId(1L);

    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class)) {
      userHolderMock.when(UserHolder::getUser).thenReturn(createUserDTO(1L));
      when(shopReviewRepository.findById(1L)).thenReturn(Optional.of(review));
      when(shopReviewRepository.findAvgRatingAndCountByShopId(1L))
          .thenReturn(new Object[]{0.0, 0L});
      when(shopService.getById(1L)).thenReturn(createShop(1L, "Shop"));

      Result result = shopReviewService.deleteReview(1L);

      assertTrue(result.getSuccess());
      verify(shopReviewRepository).deleteById(1L);
    }
  }

  private com.teatime.dto.UserDTO createUserDTO(Long id) {
    com.teatime.dto.UserDTO dto = new com.teatime.dto.UserDTO();
    dto.setId(id);
    return dto;
  }

  private User createUser(Long id, String nickName) {
    User user = new User();
    user.setId(id);
    user.setNickName(nickName);
    return user;
  }

  private Shop createShop(Long id, String name) {
    Shop shop = new Shop();
    shop.setId(id);
    shop.setName(name);
    return shop;
  }
}
