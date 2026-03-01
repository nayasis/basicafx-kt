package io.github.nayasis.kotlin.javafx.app.di.scan

import io.github.nayasis.kotlin.javafx.app.di.Inject

@Inject
class ScanInjectService {
    val name: String = "default"
}

@Inject(name = "custom")
class ScanNamedInjectService {
    val name: String = "custom"
}
