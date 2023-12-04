package com.example.movilesapp.view

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.movilesapp.R
import com.example.movilesapp.databinding.ActivityAccountsBinding
import com.example.movilesapp.databinding.ActivityNewsBinding

class NewsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        binding.backButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        binding.news1.setOnClickListener {
            openNewsDialog(R.drawable.news1,"Here’s How To Make Money Talks With Your Parents Less Awkward", "By Bernadette Joy / Nov 28, 2023", "Americans believe they need \$1.27 million to retire adequately, according to a 2023 Northwestern Mutual survey. That estimate continues to rise, up from \$1.25 million reported last year.\n" +
                    "\n" +
                    "However, the median retirement savings for all U.S. working-age households — people 32 to 61 years old — is around \$95,776, according to the Economic Policy Institute.\n" +
                    "\n" +
                    "As a first generation American from an immigrant family, I found it really tough to talk about money with my parents as they reached retirement age because of the lack of transparency around their finances, and my lack of confidence in leading tough financial conversations.")
        }

        binding.news2.setOnClickListener {
            openNewsDialog(R.drawable.news2,"You Can Afford To Invest: Start With Just \$100 A Month", "By Pattie Ehsaei / Oct 12, 2023", "Investing is one of the best ways to build wealth. Contrary to popular belief, an April study by Ramsey Solutions found only 31% of millionaires averaged \$100,000 a year throughout their career, and one-third never made six figures in any single working year. Three of four millionaires attribute their success to regular, consistent investing over an extended period.\n" +
                    "\n" +
                    "The most common pushback I receive when encouraging people to invest is, “I can’t afford it.” Many people live paycheck to paycheck and feel investing requires significant funds they don’t have.\n" +
                    "\n" +
                    "However, that couldn’t be further from the truth. You can start investing with as little as \$100 per month.\n" +
                    "\n" +
                    "You can put away \$100 with a few tweaks to your spending habits. Cutting back on your coffee habit, bringing your lunch from home, or limiting your alcohol consumption can save you at least \$25 weekly.")
        }
    }


    private fun openNewsDialog(imageResId: Int, title: String, date: String, description: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_detail_new, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val dialog = builder.create()

        val titleTextView = dialogView.findViewById<TextView>(R.id.title)
        val dateTextView = dialogView.findViewById<TextView>(R.id.date)
        val descriptionTextView = dialogView.findViewById<TextView>(R.id.description)
        val imageView = dialogView.findViewById<ImageView>(R.id.imageView)

        titleTextView.text = title
        dateTextView.text = date
        descriptionTextView.text = description
        imageView.setImageResource(imageResId)

        dialog.show()
    }


}