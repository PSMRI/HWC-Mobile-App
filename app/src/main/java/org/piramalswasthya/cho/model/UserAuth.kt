package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "USER_AUTH")
class UserAuth(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: Int,

    @ColumnInfo(name = "username")
    val userName: String,

    @ColumnInfo(name = "Password")
    val password: String,
){
    fun asDomainModel() : UserAuth{
        return UserAuth(
            userId = userId,
            userName = userName,
            password = password
        );
    }
}