package com.github.nayasis.kotlin.javafx.misc

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ImagesTest {

    @Test
    fun isAcceptable() {
        assertEquals(false, Images.isAcceptable(null) )
    }

    @Test
    fun download() {
        Images.toImage("https://ac2-p2.namu.la/20220728sac2/1ce40cda8132e008a62514d3b9b0bea85b42b58e91183a8dcac5e3cc89f8eb87.png")
        // TODO : check downloading from https url (https://ac2-p2.namu.la/20220728sac2/1ce40cda8132e008a62514d3b9b0bea85b42b58e91183a8dcac5e3cc89f8eb87.png)
    }
}