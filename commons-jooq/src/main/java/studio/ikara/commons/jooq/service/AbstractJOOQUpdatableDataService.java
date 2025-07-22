package studio.ikara.commons.jooq.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.jooq.UpdatableRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import studio.ikara.commons.configuration.service.AbstractMessageService;
import studio.ikara.commons.exception.GenericException;
import studio.ikara.commons.jooq.dao.AbstractUpdatableDAO;
import studio.ikara.commons.model.dto.AbstractUpdatableDTO;
import studio.ikara.commons.thread.VirtualThreadExecutor;

@Service
public abstract class AbstractJOOQUpdatableDataService<
        R extends UpdatableRecord<R>,
        I extends Serializable,
        D extends AbstractUpdatableDTO<I, I>,
        O extends AbstractUpdatableDAO<R, I, D>>
        extends AbstractJOOQDataService<R, I, D, O> {

    private ObjectMapper objectMapper;

    @Autowired
    private void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public CompletableFuture<D> update(I key, Map<String, Object> fields) {
        return VirtualThreadExecutor.async(() -> {
            D retrievedObject = this.read(key).join();
            Class<D> pojoClass = this.dao.getPojoClass();

            fields.forEach((field, value) -> {
                String methodName = "set" + field.substring(0, 1).toUpperCase() + field.substring(1);

                try {
                    for (Method method : pojoClass.getDeclaredMethods()) {
                        if (method.getName().equals(methodName)) {
                            Parameter[] params = method.getParameters();
                            method.invoke(retrievedObject, this.objectMapper.convertValue(value, params[0].getType()));
                        }
                    }
                } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException _) {
                    throw new GenericException(
                            HttpStatus.BAD_REQUEST, field + AbstractMessageService.FIELD_NOT_AVAILABLE);
                }
            });

            return this.update(retrievedObject).join();
        });
    }

    public CompletableFuture<D> update(D entity) {
        return VirtualThreadExecutor.async(() -> {
            D updatableEntity = this.updatableEntity(entity).join();
            I userId = this.getLoggedInUserId().join();

            if (userId != null) updatableEntity.setUpdatedBy(userId);

            return this.dao.update(updatableEntity).join();
        });
    }

    protected abstract CompletableFuture<D> updatableEntity(D entity);
}
