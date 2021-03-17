package com.victoriaBermudez.ibisdevproject

import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.sql.Connection
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

data class Snippet(val text: String)

data class DeleteCarDTO(val ownerID: String)


//data class Cars(val name: String, val precio: Float)

data class PostSnippet(val snippet: PostSnippet.Text) {
    data class Text(val text: String)
}


object Main {
    @JvmStatic
    fun main(args: Array<String>) {

        val conn: Connection? = Connect().connecting()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://server.cocoche.com.ar/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service: CocochesService = retrofit.create(CocochesService::class.java)



        embeddedServer(Netty, 8080) {
            install(ContentNegotiation) {
                jackson {
                }
            }
            routing {

                get("/cars") {
                    if (conn != null) {

                        val statement = conn.createStatement()
                        val resultSet = statement.executeQuery("SELECT * FROM cars")
                        println("car list")
                        val cars = arrayListOf<Car>()
                        while (resultSet.next()) {
                            val ownerID = resultSet.getString("ownerID")
                            val carID = resultSet.getString("carID")
                            val title = resultSet.getString("title")
                            val doors = resultSet.getInt("doors")
                            val cost = resultSet.getInt("cost")
                            val url = resultSet.getString("url")
                            val fuelType = resultSet.getString("fuelType")
                            val description = resultSet.getString("description")
                            val modelDescription = resultSet.getString("modelDescription")
                            val brandDescription = resultSet.getString("brandDescription")
                            val placeDescription = resultSet.getString("placeDescription")
                            val latitude = resultSet.getString("latitude")
                            val longitude = resultSet.getString("longitude")
                            val location = resultSet.getString("location")
                            val calificationsAvg = resultSet.getFloat("calificationsAvg")
                            val currency = resultSet.getString("currency")
                            val year = resultSet.getInt("year")
                            cars.add(
                                Car(
                                    ownerID,
                                    carID,
                                    title,
                                    doors,
                                    cost,
                                    url,
                                    fuelType,
                                    description,
                                    modelDescription,
                                    brandDescription,
                                    placeDescription,
                                    latitude,
                                    longitude,
                                    location,
                                    calificationsAvg,
                                    currency,
                                    year
                                )
                            )
                        }
                        call.respond(cars)
                        println("connected to database successfully")
                    } else {
                        // println("could not connect to database due $expt")
                    }
                }

                post("/cars") {
                    val car = call.receive(Car::class)
                    if (conn != null) {
                        val statement = conn.createStatement()
                        statement.execute("INSERT INTO cars(name, price) values (\"${car.ownerId}\", ${car.carId});")
                        call.respondText("car ${car.carId} created")
                        println("added ${car.carId} to car table")
                    } else {
                        println("could not add the car to the database")
                    }
                }

                post("/postUsers") {
                    val newUser = call.receive(User::class)
                    println("received new user with name ${newUser.userName}")

                    if (conn != null) {
                        println("connected")
                        val statement = conn.createStatement()

                        val nonExistent =
                            statement.executeQuery("SELECT email FROM users WHERE email = '${newUser.email}';")



                        if (nonExistent.next()) {
                            println("the email ${newUser.email} already exists")
                            call.respond("the email ${newUser.email} already exists")
                        } else {
                            println("hola hola hola")
                            statement.execute("CREATE USER '${newUser.userName}' IDENTIFIED BY '${newUser.passWord}' ")

                            statement.execute("GRANT ALL PRIVILEGES ON * . * TO '${newUser.userName}' ")

                            statement.execute("FLUSH PRIVILEGES;")
                            println("added new admin with name ${newUser.userName} and password ${newUser.passWord}")
                            val sql: String =
                                "INSERT INTO users(passWord, userName, phone, email, idUser, createdAt) values(?,?,?,?,?,?)"
                            val steitment = conn.prepareStatement(sql)
                            steitment.setString(1, newUser.passWord)
                            steitment.setString(2, newUser.userName)
                            steitment.setInt(3, newUser.phone)
                            steitment.setString(4, newUser.email)
                            steitment.setString(5, newUser.idUser)
                            steitment.setString(6, newUser.createdAt)
                            steitment.execute()
                            call.respond("new user added")

                        }
                    } else {
                        println("could not connect to the database")
                    }
                }

                get("/actualizarBD") {
                    val allCars = service.getAllCars(100)
                    suspendCoroutine<Unit> {
                        allCars.enqueue(object : Callback<CarListing> {
                            override fun onFailure(call: Call<CarListing>, t: Throwable) {
                                println("Falló el request a server.cocoche.com.ar. Error: $t")
                            }

                            override fun onResponse(call2: Call<CarListing>, response: Response<CarListing>) {
                                if (response.isSuccessful) {
                                    println("Request respondió satisfacoriamente!")
                                    val carListing = response.body()!!
                                    val statement = conn?.createStatement()
                                    val titleDeAutos = carListing.carList.map { it.title }
                                    println("Autos desde el post actualizarBD: \n${titleDeAutos}")
                                    statement?.execute("DELETE FROM cars")
                                    for (car in carListing.carList) {
                                        if (conn != null) {
                                            println("added ${car.carId} to car table")
                                            val sql: String =
                                                "INSERT INTO cars(ownerID, carID, title, doors, cost, url, fuelType, description, modelDescription, brandDescription, placeDescription, latitude, longitude, location, calificationsAvg, currency, year) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

                                            val stmt = conn.prepareStatement(sql)
                                            stmt.setString(1, car.ownerId)
                                            stmt.setString(2, car.carId)
                                            stmt.setString(3, car.title)
                                            stmt.setInt(4, car.doors)
                                            stmt.setInt(5, car.cost)
                                            stmt.setString(6, car.url)
                                            stmt.setString(7, car.fuelType)
                                            stmt.setString(8, car.description)
                                            stmt.setString(9, car.modelDescription)
                                            stmt.setString(10, car.brandDescription)
                                            stmt.setString(11, car.placeDescription)
                                            stmt.setString(12, car.latitude)
                                            stmt.setString(13, car.longitude)
                                            stmt.setString(14, car.location)
                                            stmt.setFloat(15, car.calificationsAvg)
                                            stmt.setString(16, car.currency)
                                            stmt.setInt(17, car.year)
                                            stmt.execute()
                                        } else {
                                            println("could not add the car to the database")
                                        }
                                    }
                                    it.resume(Unit)

                                } else {
                                    val message = response.message()
                                    println("Falló el request a server.cocoche.com.ar. Error: $message")
                                }
                            }
                        })
                    }
                    call.respondText("datos agregados")
                }





                delete("/cars") {
                    val deleteDTO = call.receive(DeleteCarDTO::class)
                    if (conn != null) {
                        val statement = conn.createStatement()
                        statement.execute("DELETE  FROM cars WHERE name = \"${deleteDTO.ownerID}\"")
                        println("deleted ${deleteDTO.ownerID} from car table ")

                    } else {
                        println("could not delete the car to the database")
                    }
                    if (conn != null) {
                        val statement = conn.createStatement()
                        val resultSet = statement.executeQuery("SELECT * FROM cars")
                        println("car list")
                        val cars = arrayListOf<Car>()
                        while (resultSet.next()) {
                            val ownerID = resultSet.getString("ownerID")
                            val carID = resultSet.getString("carID")
                            val title = resultSet.getString("title")
                            val doors = resultSet.getInt("doors")
                            val cost = resultSet.getInt("cost")
                            val url = resultSet.getString("url")
                            val fuelType = resultSet.getString("fuelType")
                            val description = resultSet.getString("description")
                            val modelDescription = resultSet.getString("modelDescription")
                            val brandDescription = resultSet.getString("brandDescription")
                            val placeDescription = resultSet.getString("placeDescription")
                            val latitude = resultSet.getString("latitude")
                            val longitude = resultSet.getString("longitude")
                            val location = resultSet.getString("location")
                            val calificationsAvg = resultSet.getFloat("calificationsAvg")
                            val currency = resultSet.getString("currency")
                            val year = resultSet.getInt("year")
                            cars.add(
                                Car(
                                    ownerID,
                                    carID,
                                    title,
                                    doors,
                                    cost,
                                    url,
                                    fuelType,
                                    description,
                                    modelDescription,
                                    brandDescription,
                                    placeDescription,
                                    latitude,
                                    longitude,
                                    location,
                                    calificationsAvg,
                                    currency,
                                    year
                                )
                            )

                        }
                        call.respond(cars)
                        println("connected to database successfully")
                    } else {
                        //  println("could not connect to database due $expt")
                    }
                }
            }
        }.start(wait = true)
    }

}