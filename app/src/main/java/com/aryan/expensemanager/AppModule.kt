package com.aryan.expensemanager


import android.content.Context
import androidx.room.Room
import com.aryan.expensemanager.data.local.ExpenseDatabase
import com.aryan.expensemanager.data.remote.CountryApi
import com.aryan.expensemanager.data.remote.ExchangeRateApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton
import kotlin.jvm.java

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ExpenseDatabase {
        return Room.databaseBuilder(
            context,
            ExpenseDatabase::class.java,
            "expense_database"
        ).build()
    }

    @Provides
    fun provideCompanyDao(database: ExpenseDatabase) = database.companyDao()

    @Provides
    fun provideUserDao(database: ExpenseDatabase) = database.userDao()

    @Provides
    fun provideExpenseDao(database: ExpenseDatabase) = database.expenseDao()

    @Provides
    fun provideApprovalRuleDao(database: ExpenseDatabase) = database.approvalRuleDao()

    @Provides
    fun provideApprovalRequestDao(database: ExpenseDatabase) = database.approvalRequestDao()

    @Provides
    @Singleton
    fun provideCountryApi(): CountryApi {
        return Retrofit.Builder()
            .baseUrl("https://restcountries.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CountryApi::class.java)
    }

    @Provides
    @Singleton
    fun provideExchangeRateApi(): ExchangeRateApi {
        return Retrofit.Builder()
            .baseUrl("https://api.exchangerate-api.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExchangeRateApi::class.java)
    }
}