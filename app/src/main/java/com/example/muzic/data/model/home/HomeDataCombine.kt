package com.example.muzic.data.model.home

import androidx.compose.runtime.Immutable
import com.example.muzic.data.model.explore.mood.Mood
import com.example.muzic.data.model.home.chart.Chart
import com.example.muzic.utils.Resource

@Immutable
data class HomeDataCombine(
    val home: Resource<ArrayList<HomeItem>>,
    val mood: Resource<Mood>,
    val chart: Resource<Chart>,
    val newRelease: Resource<ArrayList<HomeItem>>,
)