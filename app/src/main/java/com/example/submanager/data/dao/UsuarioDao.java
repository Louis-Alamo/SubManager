package com.example.submanager.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.submanager.data.model.UsuarioModel;

@Dao
public interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertUsuario(UsuarioModel usuario);

    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    UsuarioModel findByEmail(String email);
}
