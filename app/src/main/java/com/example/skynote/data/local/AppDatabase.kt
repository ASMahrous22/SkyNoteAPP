package com.example.skynote.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/* @Database(entities = [Product::class], version = 1):
 *-> It tells Room that i’m creating a database & it should use the Product class as a table.
 *-> version = 1: just means this is the first version of your database
 */
@Database(entities = [FavoriteLocation::class], version = 1)
abstract class AppDatabase : RoomDatabase()
{
    // An abstract function that the Room "RoomDatabase" will use to get access to my DAO "ProductDao".
    abstract fun favoriteDao(): FavoriteDao

    // A Singleton block to ensures that i have only one instance of the database in the whole app.
    companion object
    {
        /* @Volatile ensures that if multiple threads try to use it at the same time,
            they will see the most updated version.
         */
        @Volatile
        // "INSTANCE" is a variable that holds a single instance of the database "ProductDatabase".
        private var INSTANCE: AppDatabase? = null

        /* fun getInstance(context: Context): AppDatabase:
         *-> It is used to get the ProductDatabase instance from anywhere in the app.
         *-> It takes an Android Context as an argument.
         *-> It returns an instance of AppDatabase.
         */
        fun getDatabase(context: Context): AppDatabase
        {
            /* If INSTANCE is not null (this means that the database has been already created),
               so the function returns the existing INSTANCE.
             */
            if(INSTANCE != null)
            {
                return INSTANCE!!
            }
            /* If INSTANCE is null (this means that the database hasn't been created yet),
               the code block within the "synchronized(this)" will be executed
             */
            else
            {
                // synchronized(this) used to make sure only one thread creates the instance.
                synchronized(this)
                {
                    if(INSTANCE == null)
                    {
                        /* These lines actually build the DB "data base":
                         *-> Application context to prevent leaks & ensures DB lives as long as the app.
                         *-> Tells Room which class "ProductDatabase" to use as the DB
                         *-> Gives the DB a name: "products_db"
                         *-> .build(): Finalizes the DB configuration and creates the actual DB instance.
                         */
                        val instance = Room.databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java,
                            "skynote_database"
                        ).build()
                        /* INSTANCE = instance:
                         *-> The newly created instance is assigned to the INSTANCE variable.
                         *-> Now INSTANCE is no longer null & subsequent calls to getInstance will return it.
                         */
                        INSTANCE = instance
                    }
                    return INSTANCE!!
                }
            }
        }
    }
}


