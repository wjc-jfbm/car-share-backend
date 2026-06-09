package com.carshare.common.config.serializer;

import java.io.IOException;
import java.util.Objects;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.carshare.common.annotation.Sensitive;
import com.carshare.common.core.domain.model.LoginUser;
import com.carshare.common.enums.DesensitizedType;
import com.carshare.common.utils.SecurityUtils;

public class SensitiveJsonSerializer extends JsonSerializer<String> implements ContextualSerializer
{
    private final DesensitizedType desensitizedType;

    public SensitiveJsonSerializer()
    {
        this.desensitizedType = null;
    }

    public SensitiveJsonSerializer(DesensitizedType desensitizedType)
    {
        this.desensitizedType = desensitizedType;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException
    {
        if (desensitizedType != null && desensitization())
        {
            gen.writeString(desensitizedType.desensitizer().apply(value));
        }
        else
        {
            gen.writeString(value);
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException
    {
        Sensitive annotation = property.getAnnotation(Sensitive.class);
        if (Objects.nonNull(annotation) && Objects.equals(String.class, property.getType().getRawClass()))
        {
            return new SensitiveJsonSerializer(annotation.desensitizedType());
        }
        return prov.findValueSerializer(property.getType(), property);
    }

    private boolean desensitization()
    {
        try
        {
            LoginUser securityUser = SecurityUtils.getLoginUser();
            return !securityUser.getUser().isAdmin();
        }
        catch (Exception e)
        {
            return true;
        }
    }
}
