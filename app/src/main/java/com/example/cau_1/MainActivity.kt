package com.example.cau_1

//noinspection UsingMaterialAndMaterial3Libraries
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.cau_1.ui.theme.Cau_1Theme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.*

val poppinsMediumFont = FontFamily(Font(R.font.poppins_medium))
val poppinsRegularFont = FontFamily(Font(R.font.poppins_regular))
val robotoBold = FontFamily(Font(R.font.roboto_bold))
val robotoSemiBold = FontFamily(Font(R.font.roboto_semibold))
val robotoRegular = FontFamily(Font(R.font.roboto_regular))
val interMedium = FontFamily(Font(R.font.inter_medium))
val interBold = FontFamily(Font(R.font.inter_bold))

class MainActivity : ComponentActivity() {

    companion object {
        const val RC_SIGN_IN = 100
    }

    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // firebase auth instance
        mAuth = FirebaseAuth.getInstance()

        // configure Google SignIn
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            Cau_1Theme {

                if (mAuth.currentUser == null) {
                    GoogleSignInButton {
                        signIn()
                    }
                } else {
                    val user: FirebaseUser = mAuth.currentUser!!
                    ProfileScreen(
                        uid = user.uid,
                        profileImage = user.photoUrl!!,
                        name = user.displayName!!,
                        email = user.email!!,
                        signOutClicked = {
                            signOut()
                        }
                    )
                }

            }
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // result returned from launching the intent from GoogleSignInApi.getSignInIntent()
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val exception = task.exception
            if (task.isSuccessful) {
                try {
                    // Google SignIn was successful, authenticate with Firebase
                    val account = task.getResult(ApiException::class.java)!!
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: Exception) {
                    // Google SignIn Failed
                    Log.d("SignIn", "Google SignIn Failed")
                }
            } else {
                Log.d("SignIn", exception.toString())
            }
        }

    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // SignIn Successful
                    Toast.makeText(this, "SignIn Successful", Toast.LENGTH_SHORT).show()
                    setContent {
                        Cau_1Theme {
                            val user: FirebaseUser = mAuth.currentUser!!
                            ProfileScreen(
                                uid = user.uid,
                                profileImage = user.photoUrl!!,
                                name = user.displayName!!,
                                email = user.email!!,
                                signOutClicked = {
                                    signOut()
                                }
                            )
                        }
                    }
                } else {
                    // SignIn Failed
                    Toast.makeText(this, "SignIn Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signOut() {
        // get the google account
        val googleSignInClient: GoogleSignInClient

        // configure Google SignIn
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Sign Out of all accounts
        mAuth.signOut()
        googleSignInClient.signOut()
            .addOnSuccessListener {
                Toast.makeText(this, "Sign Out Successful", Toast.LENGTH_SHORT).show()
                setContent {
                    Cau_1Theme {
                        GoogleSignInButton {
                            signIn()
                        }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Sign Out Failed", Toast.LENGTH_SHORT).show()
            }
    }

}

@Composable
fun GoogleSignInButton(
    signInClicked: () -> Unit
) {
    Box {
        Image(
            painter = painterResource(id = R.drawable.background_uth),
            contentDescription = "background_uth",
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(513.dp, 354.dp)
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_uth),
                contentDescription = "logo_uth",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(202.dp, 197.dp)
            )

            Spacer(modifier = Modifier.padding(12.dp))

            Text(
                text = "SmartTasks",
                fontFamily = robotoSemiBold,
                fontSize = 24.sp,
                lineHeight = 30.sp,
                color = Color(0xFF2196F3),
            )

            Text(
                text = "A simple and efficient to-do app",
                fontFamily = robotoRegular,
                fontSize = 12.sp,
                lineHeight = 8.sp,
                color = Color(0xFF2196F3),
            )

            Spacer(modifier = Modifier.padding(60.dp))

            Text(
                text = "Welcome",
                fontFamily = poppinsMediumFont,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                color = Color(0xFF333333),
            )

            Spacer(modifier = Modifier.padding(8.dp))

            Text(
                text = "Ready to explore? Log in to get started.",
                fontFamily = poppinsRegularFont,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Color(0xFF333333),
            )

            Spacer(modifier = Modifier.padding(12.dp))

            Card(
                modifier = Modifier
                    .padding(start = 50.dp, end = 50.dp)
                    .height(50.dp)
                    .fillMaxWidth()
                    .clickable {
                        signInClicked()
                    },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFD5EDFF))
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.google),
                        contentDescription = "logo_gg",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(28.dp, 28.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Sign in with Google",
                        fontFamily = robotoBold,
                        fontSize = 14.sp,
                        lineHeight = 14.sp,
                        color = Color(0xFF130160),
                    )
                }
            }

            Spacer(modifier = Modifier.padding(36.dp))
        }

        Text(
            text = "© UTHSmartTasks",
            fontFamily = poppinsRegularFont,
            fontSize = 14.sp,
            lineHeight = 19.sp,
            color = Color(0xFF4A4646),
            modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uid: String,
    profileImage: Uri,
    name: String,
    email: String,
    signOutClicked: () -> Unit
) {
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val dateState = remember { mutableStateOf("") }
    val showDialog = remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
        Button(
            onClick = { signOutClicked() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
            )
        ) {
            Image(
                painter = painterResource(id = R.drawable.chevron_left),
                contentDescription = "Chevron_left",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(40.dp, 40.dp)
            )
        }

        Text(
            text = "Profile",
            fontFamily = robotoSemiBold,
            fontSize = 30.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 37.5.sp,
            color = Color(0xFF2196F3),
            modifier = Modifier.align(Alignment.TopCenter).padding(9.dp)
        )


        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .size(150.dp)
                    .fillMaxHeight(0.4f),
                shape = RoundedCornerShape(125.dp),
            ) {
                AsyncImage(
                    modifier = Modifier.fillMaxSize(),
                    model = profileImage,
                    contentDescription = "profile_photo",
                    contentScale = ContentScale.FillBounds
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxHeight(0.6f)
                    .padding(top = 60.dp)
                    .padding(start = 24.dp, end = 24.dp)
            ) {
                Text(
                    text = "Name",
                    fontFamily = interBold,
                    fontSize = 16.sp,
                    lineHeight = 14.sp,
                    color = Color(0xFF000000),
                )

                Spacer(modifier = Modifier.padding(6.dp))

                Row(
                    modifier = Modifier
                        .height(44.dp)
                        .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                        .fillMaxWidth(),
                ) {
                    Text(
                        text = name,
                        fontFamily = interMedium,
                        fontSize = 14.sp,
                        lineHeight = 12.sp,
                        color = Color(0xFF000000),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }

                Spacer(modifier = Modifier.padding(8.dp))

                Text(
                    text = "Email",
                    fontFamily = interBold,
                    fontSize = 16.sp,
                    lineHeight = 14.sp,
                    color = Color(0xFF000000),
                )

                Spacer(modifier = Modifier.padding(6.dp))

                Row(
                    modifier = Modifier
                        .height(44.dp)
                        .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                        .fillMaxWidth(),
                ) {
                    Text(
                        text = email,
                        fontFamily = interMedium,
                        fontSize = 14.sp,
                        lineHeight = 12.sp,
                        color = Color(0xFF000000),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }

                Spacer(modifier = Modifier.padding(8.dp))

                Text(
                    text = "Date of Birth",
                    fontFamily = interBold,
                    fontSize = 16.sp,
                    lineHeight = 14.sp,
                    color = Color(0xFF000000),
                )

                Spacer(modifier = Modifier.padding(6.dp))

                Row(
                    modifier = Modifier
                        .height(44.dp)
                        .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    getBirthDateFromFirestore(uid) { birthDate ->
                        dateState.value = birthDate
                    }
                    Text(
                        text = dateState.value,
                        fontFamily = interMedium,
                        fontSize = 14.sp,
                        lineHeight = 12.sp,
                        color = Color(0xFF000000),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )

                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "ArrowDropDown",
                        modifier = Modifier.align(Alignment.CenterVertically)
                            .clickable { showDialog.value = true }
                    )
                }
            }
        }

        Button(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp)
                .fillMaxWidth(),
            onClick = { signOutClicked() }
        ) {
            Text(
                text = "Back",
                fontFamily = robotoBold,
                fontSize = 20.sp,
                lineHeight = 25.sp,
                color = Color(0xFFFFFFFF),
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }

    if (showDialog.value) {
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showDialog.value = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedDateMillis = datePickerState.selectedDateMillis
                    if (selectedDateMillis != null) {
                        dateState.value = dateFormatter.format(Date(selectedDateMillis))
                        saveBirthDateToFirestore(uid, dateState.value)
                    }
                    showDialog.value = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog.value = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

fun saveBirthDateToFirestore(userId: String, birthDate: String) {
    val db = FirebaseFirestore.getInstance()
    val userData = hashMapOf(
        "birthDate" to birthDate
    )

    val userRef = db.collection("users").document(userId)
    userRef.set(userData, SetOptions.merge())
        .addOnSuccessListener {
            Log.d("Firestore", "Ngày sinh được lưu thành công!")
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Lỗi khi lưu ngày sinh", e)
        }
}

fun getBirthDateFromFirestore(userId: String, onResult: (String) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val userRef = db.collection("users").document(userId)

    userRef.get()
        .addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val birthDate = document.getString("birthDate") ?: ""
                onResult(birthDate)
            }
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Lỗi khi lấy ngày sinh", e)
        }
}