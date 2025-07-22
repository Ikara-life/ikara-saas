package studio.ikara.commons.jooq.controller;

import studio.ikara.commons.jooq.dao.AbstractUpdatableDAO;
import studio.ikara.commons.jooq.service.AbstractJOOQUpdatableDataService;
import studio.ikara.commons.model.dto.AbstractUpdatableDTO;
import org.jooq.UpdatableRecord;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractJOOQUpdatableDataController<
                R extends UpdatableRecord<R>,
                I extends Serializable,
                D extends AbstractUpdatableDTO<I, I>,
                O extends AbstractUpdatableDAO<R, I, D>,
                S extends AbstractJOOQUpdatableDataService<R, I, D, O>>
        extends AbstractJOOQDataController<R, I, D, O, S> {

    @PutMapping(AbstractJOOQDataController.PATH_ID)
    public CompletableFuture<ResponseEntity<D>> put(
            @PathVariable(name = PATH_VARIABLE_ID, required = false) final I id, @RequestBody D entity) {
        if (id != null) entity.setId(id);
        return this.service.update(entity).thenApply(ResponseEntity::ok);
    }

    @PatchMapping(AbstractJOOQDataController.PATH_ID)
    public CompletableFuture<ResponseEntity<D>> patch(
            @PathVariable(name = PATH_VARIABLE_ID) final I id, @RequestBody Map<String, Object> entityMap) {
        return this.service.update(id, entityMap).thenApply(ResponseEntity::ok);
    }
}
