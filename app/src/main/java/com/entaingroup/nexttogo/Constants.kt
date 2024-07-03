package com.entaingroup.nexttogo

const val API_BASE_URL = "https://api.neds.com.au/"
const val DEFAULT_RACE_DISPLAY_COUNT = 5
/** Countdown starts when race starts in less than this many seconds */
const val RACE_COUNTDOWN_DURATION_SECONDS = 60L
/** Countdown stops when race started this many seconds ago */
const val RACE_COUNTDOWN_ELAPSE_LIMIT_SECONDS = -60L
const val TIME_SECONDS_IN_MINUTE = 60
const val TIME_SECONDS_IN_HOUR = TIME_SECONDS_IN_MINUTE * 60
const val TIME_SECONDS_IN_DAY = TIME_SECONDS_IN_HOUR * 24