package com.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;

@Converter
public class VectorConverter implements AttributeConverter<float[], String> {

    @Override
    public String convertToDatabaseColumn(float[] attribute) {
        if (attribute == null || attribute.length == 0) {
            return null;
        }
        return Arrays.toString(attribute); // Chuyển [1.0, 2.0] thành "[1.0, 2.0]"
    }

    @Override
    public float[] convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        // Xóa dấu ngoặc [] và split theo dấu phẩy
        String[] s = dbData.substring(1, dbData.length() - 1).split(",");
        float[] res = new float[s.length];
        for (int i = 0; i < s.length; i++) {
            res[i] = Float.parseFloat(s[i].trim());
        }
        return res;
    }
}