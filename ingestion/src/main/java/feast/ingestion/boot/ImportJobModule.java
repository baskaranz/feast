/*
 * Copyright 2018 The Feast Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package feast.ingestion.boot;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.util.List;
import org.apache.beam.sdk.options.PipelineOptions;
import feast.ingestion.model.Specs;
import feast.ingestion.model.SpecsImpl;
import feast.ingestion.options.ImportJobOptions;
import feast.ingestion.service.CachedSpecService;
import feast.ingestion.service.CoreSpecService;
import feast.ingestion.service.FileSpecService;
import feast.ingestion.service.SpecService.Builder;
import feast.ingestion.service.SpecService.UnsupportedBuilder;
import feast.specs.ImportSpecProto.ImportSpec;
import feast.storage.ErrorsStore;
import feast.storage.ServingStore;
import feast.storage.WarehouseStore;
import feast.storage.service.ErrorsStoreService;
import feast.storage.service.ServingStoreService;
import feast.storage.service.WarehouseStoreService;

/** An ImportJobModule is a Guice module for creating dependency injection bindings. */
public class ImportJobModule extends AbstractModule {

  private final ImportJobOptions options;
  private ImportSpec importSpec;

  public ImportJobModule(ImportJobOptions options, ImportSpec importSpec) {
    this.options = options;
    this.importSpec = importSpec;
  }

  @Override
  protected void configure() {
    bind(ImportJobOptions.class).toInstance(options);
    bind(PipelineOptions.class).toInstance(options);
    bind(ImportSpec.class).toInstance(importSpec);
    bind(Specs.class).to(SpecsImpl.class);
  }

  @Provides
  @Singleton
  Builder provideSpecService(ImportJobOptions options) {
    if (options.getCoreApiUri() != null) {
      return new CachedSpecService.Builder(new CoreSpecService.Builder(options.getCoreApiUri()));
    } else if (options.getCoreApiSpecPath() != null) {
      return new CachedSpecService.Builder(
          new FileSpecService.Builder(options.getCoreApiSpecPath()));
    } else {
      return new UnsupportedBuilder(
          "Cannot initialise spec service as coreApiHost or specPath was not set.");
    }
  }

  @Provides
  @Singleton
  List<WarehouseStore> provideWarehouseStores() {
    return WarehouseStoreService.getAll();
  }

  @Provides
  @Singleton
  List<ServingStore> provideServingStores() {
    return ServingStoreService.getAll();
  }

  @Provides
  @Singleton
  List<ErrorsStore> provideErrorsStores() {
    return ErrorsStoreService.getAll();
  }
}
