package com.flyppcorp.flypp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.flyppcorp.atributesClass.LastMessage
import com.flyppcorp.atributesClass.Servicos
import com.flyppcorp.constants.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_last_messages.*
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.last_messages_layout.*
import kotlinx.android.synthetic.main.last_messages_layout.view.*

class LastMessagesActivity : AppCompatActivity() {

   private lateinit var mFirestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_last_messages)
    }
}
