package com.DTO;

import lombok.*;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BannerConfigDTO {
    private List<BannerSlotDTO> banners;
    private List<BannerSlotDTO> categories;
    private List<BannerSlotDTO> quickTopOption;
    private List<BannerSlotDTO> quickBottomOption;
    private List<Long> featuredPostIds;
    private List<Long> featuredPopularIds;
    private List<PromoPost> saleEvents;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class BannerSlotDTO {
        private String id;
        private String type;   // main | side | category
        private String label;
        private String image;
        private String title;
        private String subtitle;
        private String badge;
        private String link;
        private String icon;
        private List<BannerSlotChildDTO> children;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PromoPost {
        private String id;
        private String label;
        private String link;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class BannerSlotChildDTO {
        private String id;
        private String label;
        private String link;
        private String icon;
    }
}