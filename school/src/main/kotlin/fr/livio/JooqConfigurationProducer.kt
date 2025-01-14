package fr.livio

import io.agroal.api.AgroalDataSource
import jakarta.enterprise.inject.Produces
import org.jooq.Configuration
import org.jooq.SQLDialect
import org.jooq.conf.RenderNameCase
import org.jooq.conf.RenderQuotedNames
import org.jooq.conf.Settings
import org.jooq.impl.DefaultConfiguration

class JooqConfigurationProducer {

    @Produces
    fun getConfiguration(dataSource: AgroalDataSource): Configuration = DefaultConfiguration()
        .set(dataSource)
        .set(SQLDialect.POSTGRES)
        .set(
            Settings()
                .withExecuteLogging(true)
                .withRenderFormatted(true)
                .withRenderCatalog(false)
                .withRenderSchema(false)
                .withMaxRows(Integer.MAX_VALUE)
                .withRenderQuotedNames(RenderQuotedNames.EXPLICIT_DEFAULT_UNQUOTED)
                .withRenderNameCase(RenderNameCase.LOWER_IF_UNQUOTED)
        )
}