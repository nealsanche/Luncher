package co.touchlab.kampstarter.models

import com.squareup.sqldelight.ColumnAdapter

expect class Date(year: Int, month: Int, day: Int)

expect class DateAdapter() : ColumnAdapter<Date, Long>