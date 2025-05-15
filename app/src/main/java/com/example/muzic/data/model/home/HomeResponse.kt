package com.example.muzic.data.model.home

import androidx.compose.runtime.Immutable
import com.example.muzic.data.model.explore.mood.Mood
import com.example.muzic.data.model.home.chart.Chart
import com.example.muzic.utils.Resource

@Immutable
data class HomeResponse(
    val homeItem: com.example.muzic.utils.Resource<ArrayList<HomeItem>>,
    val exploreMood: com.example.muzic.utils.Resource<Mood>,
    val exploreChart: com.example.muzic.utils.Resource<Chart>,
)