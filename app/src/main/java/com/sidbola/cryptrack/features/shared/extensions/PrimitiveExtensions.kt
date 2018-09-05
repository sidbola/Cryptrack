package com.sidbola.cryptrack.features.shared.extensions

fun Double.specialFormat(desiredDigits: Int): String {

    var digits = desiredDigits
    var formattedString = java.lang.String.format("%.${digits}f", this)
    var decimals = formattedString.substringAfter(".")

    if (decimals.contains(Regex("[1-9]"))) {
        while (formattedString.last() == '0') {
            formattedString = formattedString.dropLast(1)
        }

        decimals = formattedString.substringAfter(".")

        while (decimals.length < desiredDigits) {
            formattedString += "0"
            decimals = formattedString.substringAfter(".")
        }
        return insertCommas(formattedString)
    }

    digits++

    while (true) {
        formattedString = java.lang.String.format("%.${digits}f", this)

        if (formattedString.last() != '0') {
            break
        }

        if (formattedString.last() == '0') {
            digits++
        }

        if (digits == 20) {
            break
        }
    }

    decimals = formattedString.substringAfter(".")
    if (!decimals.contains(Regex("[1-9]"))) {
        formattedString = formattedString.dropLastWhile { it == '0' }
        formattedString = formattedString.dropLast(1)
    }

    return insertCommas(formattedString)
}

fun insertCommas(number: String): String {
    val wholeNumber: String
    var decimalNumber = ""
    var numberWithCommas: String

    if (number.contains(".")) {
        wholeNumber = number.substringBefore(".")
        numberWithCommas = number.substringBefore(".")
        decimalNumber = "." + number.substringAfter(".")
    } else {
        wholeNumber = number
        numberWithCommas = number
    }

    for (i in wholeNumber.length - 3 downTo 0 step 3) {
        numberWithCommas = numberWithCommas.substring(0, i) + "," + numberWithCommas.substring(i, numberWithCommas.length)
    }

    if (numberWithCommas.first() == ',') {
        numberWithCommas = numberWithCommas.drop(1)
    }

    if (numberWithCommas.first() == '-'){
        if (numberWithCommas[1] == ','){
            numberWithCommas = numberWithCommas.drop(2)
            numberWithCommas = "-$numberWithCommas"
        }
    }

    return numberWithCommas + decimalNumber
}