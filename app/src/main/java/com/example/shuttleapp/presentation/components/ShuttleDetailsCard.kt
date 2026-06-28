package com.example.shuttleapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AirportShuttle
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Room
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shuttleapp.ui.theme.NavyBlue
import com.example.shuttleapp.ui.theme.White
import com.example.shuttleapp.util.formatTimeHHmm

//ShuttleDetailsCard
@Composable
fun ShuttleDetailsCard(
    route: String,
    departure: String,
    arrival: String,
    driver: String,
    plateNumber: String,
    shuttleProvider: String,
    tripType: String) {

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(color = NavyBlue, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically

        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Room,
                        tint = White,
                        contentDescription = "navigation icon",
                        modifier = Modifier.size(20.dp))

                    VerticalDivider(Modifier.height(24.dp))

                    Icon(
                        imageVector = Icons.Outlined.Room,
                        tint = White,
                        contentDescription = "navigation icon",
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.Center) {
                    Row(
                        modifier = Modifier.padding(start = 12.dp)
                    ){
                        Column(
                            horizontalAlignment = Alignment.Start
                        ) {
                            AutoResizeText("From", colors = Color.LightGray, fontSize = 12.sp)
                            AutoResizeText(
                                if (tripType == "Incoming") route else "Pick-Up Point",
                                colors = White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )

                        }

                        Spacer(Modifier.weight(1f))

                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            AutoResizeText("Departure", colors = Color.LightGray, fontSize = 12.sp)
                            AutoResizeText(
                                formatTimeHHmm(departure),
                                colors = White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )

                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    ShuttleCardDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.padding(start = 12.dp)
                    ){
                        Column(
                            horizontalAlignment = Alignment.Start
                        ) {
                            AutoResizeText("To", colors = Color.LightGray, fontSize = 12.sp)
                            AutoResizeText(
                                if (tripType == "Outgoing") route else "Drop-off Point",
                                colors = White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )

                        }

                        Spacer(Modifier.weight(1f))

                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            AutoResizeText("Arrival", colors = Color.LightGray, fontSize = 12.sp)
                            AutoResizeText(
                                formatTimeHHmm(arrival),
                                colors = White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )

                        }
                    }
                }
            }


        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Person,
                tint = White,
                contentDescription = "navigation icon",
                modifier = Modifier.size(20.dp)
            )

            Spacer(Modifier.width(12.dp))

            AutoResizeText(driver, colors = White, fontSize = 16.sp)

        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.AirportShuttle,
                tint = White,
                contentDescription = "navigation icon",
                modifier = Modifier.size(20.dp)
            )

            Spacer(Modifier.width(12.dp))
            AutoResizeText("Plate Number:", colors = Color.LightGray, fontSize = 12.sp)
            Spacer(Modifier.weight(0.05f))
            AutoResizeText(plateNumber, colors = White, fontSize = 16.sp)
            Spacer(Modifier.weight(0.90f))
            AutoResizeText("Provider:", colors = Color.LightGray, fontSize = 12.sp)
            Spacer(Modifier.weight(0.05f))
            AutoResizeText(shuttleProvider, colors = White, fontSize = 16.sp)
        }
    }
}

@Composable
fun ShuttleCardDivider() {
    HorizontalDivider(
        modifier = Modifier.fillMaxWidth().padding(start = 12.dp),
        thickness = 1.dp,
        color = White
    )
}