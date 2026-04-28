package com.poxiao.app.campus

import com.poxiao.app.data.AcademicGateway
import com.poxiao.app.data.CampusLocation
import com.poxiao.app.data.CampusMapGateway
import com.poxiao.app.data.FeedCard
import com.poxiao.app.data.MarketplaceGateway
import com.poxiao.app.data.PreviewAcademicGateway
import com.poxiao.app.data.PreviewCampusMapGateway
import com.poxiao.app.data.PreviewMarketplaceGateway

data class CampusSnapshot(
    val emptyClassrooms: List<FeedCard>,
    val grades: List<FeedCard>,
    val books: List<FeedCard>,
    val location: CampusLocation?,
)

class CampusServicesRepository(
    private val academicGateway: AcademicGateway = PreviewAcademicGateway(),
    private val campusMapGateway: CampusMapGateway = PreviewCampusMapGateway(),
    private val marketplaceGateway: MarketplaceGateway = PreviewMarketplaceGateway(),
) {
    suspend fun loadSnapshot(userToken: String = ""): CampusSnapshot {
        val emptyClassrooms = academicGateway.getEmptyClassrooms("today")
        val grades = academicGateway.getGrades()
        val books = marketplaceGateway.listBooks()
        val location = campusMapGateway.locateMe()
        return CampusSnapshot(
            emptyClassrooms = emptyClassrooms,
            grades = grades,
            books = books,
            location = location,
        )
    }
}
