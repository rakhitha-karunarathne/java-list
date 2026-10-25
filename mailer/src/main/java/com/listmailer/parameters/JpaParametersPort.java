package com.listmailer.parameters;

import com.listmailer.kernel.ports.ParametersPort;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class JpaParametersPort implements ParametersPort {

  private final ParameterRepository repository;

  public JpaParametersPort(ParameterRepository repository) {
    this.repository = repository;
  }

  @Override
  public Optional<String> get(String key) {
    return repository.findById(key).map(ParameterEntity::getValue);
  }

  @Override
  @Transactional
  public void set(String key, String value) {
    ParameterDefinitions.Def def = ParameterDefinitions.require(key);
    ParameterEntity row =
        repository
            .findById(key)
            .orElseGet(() -> new ParameterEntity(key, value, def.description()));
    row.setValue(value);
    row.setDescription(def.description());
    repository.save(row);
  }
}
