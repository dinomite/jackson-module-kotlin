package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.SingletonSupport
import com.fasterxml.jackson.module.kotlin.SingletonSupport.CANONICALIZE
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@type")
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator::class, property = "@id")
interface Entity

object NumberEntity : Entity

data class NumberValue(val value: Int) {
    val entity = NumberEntity
}

class TestGithubX {
    private val mapper: ObjectMapper = ObjectMapper()
        .registerModule(KotlinModule(singletonSupport = CANONICALIZE))

    private val json = """
        [ {
          "value" : 10,
          "entity" : {
            "@type" : ".NumberEntity",
            "@id" : 1
          }
        }, {
          "value" : 11,
          "entity" : 1
        } ]
        """.trimIndent()

    @Test
    fun `test writing involving type, id and object`() {
        val input = listOf(NumberValue(10), NumberValue(11))

        val output = mapper
            .writerFor(jacksonTypeRef<List<NumberValue>>())
            .withDefaultPrettyPrinter()
            .writeValueAsString(input)

        assertEquals(json, output)
    }

    @Test
    fun `test reading involving type, id and object`() {
        val output = mapper.readValue<List<NumberValue>>(json)

        assertEquals(2, output.size)
        val (a, b) = output
        assertSame(NumberEntity::class.java, a.entity.javaClass)
        assertSame(NumberEntity::class.java, b.entity.javaClass)
        assertEquals(10, a.value)
        assertEquals(11, b.value)
    }
}
