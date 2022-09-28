package io.github.oxidefrp.oxide.core

import kotlin.test.Test
import kotlin.test.assertEquals

private data class WriteResult(
    val newContent: String,
)

private class FakeFilesystem(
    files: Map<String, String>,
) {
    private val files: MutableMap<String, String> = files.toMutableMap()

    fun readFile(fileName: String): Io<String?> = object : Io<String?>() {
        override fun performExternally(): String? = files[fileName]
    }

    fun writeFile(
        fileName: String,
        content: String,
    ): Io<WriteResult> = object : Io<WriteResult>() {
        override fun performExternally(): WriteResult {
            files[fileName] = content

            return WriteResult(
                newContent = content,
            )
        }
    }

    fun appendToFile(
        fileName: String,
        suffix: String,
    ): Io<WriteResult> = readFile(fileName = fileName).performOf { oldContent ->
        writeFile(
            fileName = fileName,
            content = (oldContent ?: "") + suffix,
        )
    }
}

class MomentIoOperatorsUnitTests {
    @Test
    fun testPure() {
        val moment = MomentIo.pure(8)

        assertEquals(
            expected = 8,
            actual = moment.performExternally(),
        )
    }

    @Test
    fun testLift() {
        val filesystem = FakeFilesystem(
            files = mapOf(
                "file1.txt" to "foo",
                "file2.txt" to "bar",
                "file3.txt" to "baz",
            )
        )

        val fileId = MutableCell(1)

        val appendSuffix = MomentIo.lift(
            fileId.sample().map {
                filesystem.appendToFile(
                    fileName = "file$it.txt",
                    suffix = "-append1"
                )
            }
        )

        assertEquals(
            expected = WriteResult(
                newContent = "foo-append1",
            ),
            actual = appendSuffix.performExternally(),
        )

        assertEquals(
            expected = "foo-append1",
            actual = filesystem.readFile(
                fileName = "file1.txt",
            ).performExternally(),
        )

        fileId.setValueExternally(2)

        assertEquals(
            expected = WriteResult(
                newContent = "bar-append1",
            ),
            actual = appendSuffix.performExternally(),
        )

        assertEquals(
            expected = "bar-append1",
            actual = filesystem.readFile(
                fileName = "file2.txt",
            ).performExternally(),
        )
    }

    @Test
    fun testMap() {
        val filesystem = FakeFilesystem(
            files = mapOf(
                "file1.txt" to "foo",
                "file2.txt" to "bar",
                "file3.txt" to "baz",
            )
        )

        val fileId = MutableCell(1)

        val appendSuffix = MomentIo.lift(
            fileId.sample().map {
                filesystem.appendToFile(
                    fileName = "file$it.txt",
                    suffix = "-append1"
                )
            }
        )

        val result = appendSuffix.map {
            it.newContent.uppercase()
        }

        assertEquals(
            expected = "FOO-APPEND1",
            actual = result.performExternally(),
        )

        assertEquals(
            expected = "foo-append1",
            actual = filesystem.readFile(
                fileName = "file1.txt",
            ).performExternally(),
        )

        fileId.setValueExternally(2)

        assertEquals(
            expected = "BAR-APPEND1",
            actual = result.performExternally(),
        )

        assertEquals(
            expected = "bar-append1",
            actual = filesystem.readFile(
                fileName = "file2.txt",
            ).performExternally(),
        )
    }

    @Test
    fun testApply() {
        fun <A, B, C> map2(
            sa: MomentIo<A>,
            sb: MomentIo<B>,
            f: (a: A, b: B) -> C,
        ): MomentIo<C> {
            fun g(a: A) = fun(b: B) = f(a, b)

            return MomentIo.apply(
                sa.map(::g),
                sb,
            )
        }

        val filesystem = FakeFilesystem(
            files = mapOf(
                "file1.txt" to "foo",
                "file2.txt" to "bar",
                "file3.txt" to "baz",
            )
        )

        val fileId = MutableCell(1)

        val appendSuffixA: MomentIo<WriteResult> = MomentIo.lift(
            fileId.sample().map {
                filesystem.appendToFile(
                    fileName = "file$it.txt",
                    suffix = "-appendA"
                )
            }
        )

        val appendSuffixB: MomentIo<WriteResult> = MomentIo.lift(
            fileId.sample().map {
                filesystem.appendToFile(
                    fileName = "file$it.txt",
                    suffix = "-appendB",
                )
            }
        )

        val result = map2(
            appendSuffixA,
            appendSuffixB,
        ) { resultA, resultB ->
            "${resultA.newContent}:${resultB.newContent}"
        }

        assertEquals(
            expected = "foo-appendA:foo-appendA-appendB",
            actual = result.performExternally(),
        )

        assertEquals(
            expected = "foo-appendA-appendB",
            actual = filesystem.readFile(
                fileName = "file1.txt",
            ).performExternally(),
        )
    }

    @Test
    fun testPerform() {
        fun <A, B> performOf(
            momentIo: MomentIo<A>,
            transform: (A) -> MomentIo<B>,
        ): MomentIo<B> =
            MomentIo.perform(momentIo.map(transform))

        val filesystem = FakeFilesystem(
            files = mapOf(
                "pointer1.txt" to "file1.txt",
                "pointer2.txt" to "file2.txt",
                "file1.txt" to "bar",
                "file2.txt" to "baz",
            )
        )

        val pointerId = MutableCell(1)
        val preparedSuffix = MutableCell("suffix1")

        val readFileName: MomentIo<String?> = MomentIo.lift(
            pointerId.sample().map {
                filesystem.readFile(
                    fileName = "pointer$it.txt",
                )
            }
        )

        val result: MomentIo<WriteResult> = performOf(readFileName) { fileName ->
            MomentIo.lift(
                preparedSuffix.sample().map { suffix ->
                    filesystem.appendToFile(
                        fileName = fileName!!,
                        suffix = "-$suffix",
                    )
                }
            )
        }

        assertEquals(
            expected = WriteResult(newContent = "bar-suffix1"),
            actual = result.performExternally(),
        )

        assertEquals(
            expected = "bar-suffix1",
            actual = filesystem.readFile(
                fileName = "file1.txt",
            ).performExternally(),
        )

        assertEquals(
            expected = "baz",
            actual = filesystem.readFile(
                fileName = "file2.txt",
            ).performExternally(),
        )
    }
}
