package com.victoriaBermudez.ibisdevproject
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException


class Connect {
    fun connecting(): Connection? {
        var conn: Connection? = null
        var expt: String? = null
        try {
            Class.forName("com.mysql.cj.jdbc.Driver")
            conn = DriverManager.getConnection("jdbc:mysql://localhost/fordcars?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", "vicky", "1234")
            return conn
        } catch (se: SQLException) {
            se.printStackTrace()
            expt = se.message
        } catch (e: Exception) {
            e.printStackTrace()
            expt = e.message
        }

        return conn
    }


}