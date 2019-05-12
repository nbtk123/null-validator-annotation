package com.nir.app

import com.nullvalidateannotation.NullValidatorClass
import com.nullvalidateannotation.NullValidatorField
import validate

fun main(args: Array<String>) {
    val user = User("0", "Nir", "Barzilay",null)
    if (user.validate()) {
        println("Validated!")
    } else {
        println("Not validated!")
    }
}

@NullValidatorClass
data class User(
    @NullValidatorField val id: String,
    @NullValidatorField val firstName: String,
    @NullValidatorField val lastName: String,
    val email: String?
)