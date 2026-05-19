package com.DTO;

import com.DTO.sections.*;
import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomePageConfigDTO {
    private List<BannerSlotDTO> banners;
    private List<BannerSlotDTO> navCategories;
    private List<BannerSlotDTO> navQuickTopOption;
    private List<BannerSlotDTO> navQuickBottomOption;
    private List<PromoPost> saleEvents;

    // Các section riêng biệt
    private FeaturedProductConfig featuredProducts;
    private NewProductConfig newProducts;
    private BrandsShowcaseConfig brandShowcase;
    private HotDealsSectionConfig hotDeals;
    private NewsSectionConfig news;
    private List<CategoryConfig> categorySections;
    private FeaturedCategoryConfig featuredCategories;
    private List<String> layout;
    private FooterConfigDTO footer;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BannerSlotDTO {
        private String id;
        private String type; // main | side | category
        private String label;
        private String image;
        private String title;
        private String subtitle;
        private String badge;
        private String link;
        private String icon;
        private List<BannerSlotChildDTO> children;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PromoPost {
        private Long id;
        private String label;
        private String link;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BannerSlotChildDTO {
        private String id;
        private String label;
        private String link;
        private String icon;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FooterConfigDTO {
        private String companyDescription;
        private String address;
        private String hotline;
        private String email;
        private String workingHours;
        private String copyright;
        private String facebookLink;
        private String twitterLink;
        private String instagramLink;
        private String youtubeLink;
        private String shopeeLink;
        private String lazadaLink;
        private String tiktokLink;
    }
}
