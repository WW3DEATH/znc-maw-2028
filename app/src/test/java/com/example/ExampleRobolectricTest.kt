package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ZSP - 28", appName)
  }

  @Test
  fun `verify admin login and stream locking`() {
    val res = com.example.data.DataRepository.login("mnmjaasim@gmail.com", "jaasim2010")
    assertEquals(true, res.isSuccess)
    val user = res.getOrNull()
    assertEquals(com.example.model.UserRole.ADMIN, user?.role)
    assertEquals(true, user?.isStreamLocked)
  }
}
