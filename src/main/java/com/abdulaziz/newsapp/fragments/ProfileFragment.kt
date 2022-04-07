package com.abdulaziz.newsapp.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.abdulaziz.newsapp.MainActivity
import com.abdulaziz.newsapp.database.AppDatabase
import com.abdulaziz.newsapp.databinding.FragmentProfileBinding
import com.abdulaziz.newsapp.fragments.SignInFragment.Companion.CLIENT_ID
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {


    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        (activity as MainActivity).showBottomNavBar()

        val currentUser = FirebaseAuth.getInstance().currentUser

        database = AppDatabase.getInstance(requireContext())
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(CLIENT_ID)
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        if (currentUser != null) {
            Picasso.get().load(currentUser.photoUrl).into(binding.avatarIv)
            binding.displayNameTv.text = currentUser.displayName
            binding.phoneNumberTv.text = currentUser.phoneNumber
            binding.signOutBtn.setOnClickListener {
                val alertDialog = AlertDialog.Builder(requireContext())
                alertDialog.setTitle("Do you really want to leave your profile?")
                alertDialog.setPositiveButton("Yes"
                ) { p0, p1 ->
                    googleSignInClient.signOut()
                    FirebaseAuth.getInstance().signOut()
                    database.clearAllTables()
                    (activity as MainActivity).finish()
                    startActivity((activity as MainActivity).intent)
                }
                alertDialog.setNegativeButton("Cancel") { p0, p1 -> }
                alertDialog.show()
            }
        }

        return binding.root
    }

    override fun onStop() {
        super.onStop()
        (activity as MainActivity).hideBottomNavBar()
    }


}