package io.newm.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.newm.LocalIsBottomBarVisible
import io.newm.core.resources.R
import io.newm.core.theme.Black
import io.newm.core.theme.Black90
import io.newm.core.theme.DarkViolet
import io.newm.core.theme.Pinkish
import io.newm.core.theme.Purple
import io.newm.core.theme.White
import io.newm.core.theme.inter
import io.newm.core.ui.utils.iconGradient
import io.newm.shared.public.analytics.NewmAppEventLogger
import io.newm.shared.public.analytics.events.AppScreens


private val buttonGradient =
    iconGradient(DarkViolet.copy(alpha = 0.08f), Pinkish.copy(alpha = 0.08f))

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SongFilterBottomSheet(
    sheetState: ModalBottomSheetState,
    filters: NFTLibraryFilters,
    onApplyFilters: (NFTLibraryFilters) -> Unit,
    eventLogger: NewmAppEventLogger
) {
    LocalIsBottomBarVisible.current.value = sheetState.targetValue != ModalBottomSheetValue.Expanded
    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            if (sheetState.targetValue == ModalBottomSheetValue.Expanded) {
                LaunchedEffect(Unit) {
                    eventLogger.logPageLoad(AppScreens.NFTLibraryFilterScreen.name)
                }
            }
            Box(
                modifier = Modifier
                    .background(Black90)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colors.surface,
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.library_filter_songs),
                        style = TextStyle(
                            fontFamily = inter,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = White
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SongFilterButton(
                        onClick = { onApplyFilters(filters.copy(showShortTracks = !filters.showShortTracks)) },
                        labelRes = R.string.library_filter_songs_under_30,
                        isSelected = filters.showShortTracks
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                    Text(
                        text = stringResource(id = R.string.library_sort_songs),
                        style = TextStyle(
                            fontFamily = inter,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = White
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SongFilterButton(
                        onClick = { onApplyFilters(filters.selectSortType(NFTLibrarySortType.ByTitle)) },
                        labelRes = R.string.library_sort_by_title,
                        isSelected = filters.sortType == NFTLibrarySortType.ByTitle
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SongFilterButton(
                        onClick = { onApplyFilters(filters.selectSortType(NFTLibrarySortType.ByArtist)) },
                        labelRes = R.string.library_sort_by_artist,
                        isSelected = filters.sortType == NFTLibrarySortType.ByArtist
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SongFilterButton(
                        onClick = { onApplyFilters(filters.selectSortType(NFTLibrarySortType.ByLength)) },
                        labelRes = R.string.library_sort_by_length,
                        isSelected = filters.sortType == NFTLibrarySortType.ByLength
                    )
                }
            }
        },
        scrimColor = Black90,
        content = { }
    )
}

@Composable
private fun SongFilterButton(
    onClick: () -> Unit,
    labelRes: Int,
    isSelected: Boolean
) {
    val modifier = Modifier
        .clip(RoundedCornerShape(8.dp))
        .fillMaxWidth()
        .height(40.dp)
    Button(
        onClick = onClick,
        modifier = if (isSelected) modifier.background(Purple) else modifier.background(
            buttonGradient
        ),
        elevation = null,
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent)
    )
    {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(id = labelRes),
                fontFamily = inter,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = if (isSelected) Black else Purple,
            )
            Spacer(modifier = Modifier.weight(1f))
            if (isSelected) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_library_filter_check),
                    contentDescription = stringResource(R.string.check_icon_description),
                    tint = Black,
                )
            }
        }
    }
}

private fun NFTLibraryFilters.selectSortType(sortType: NFTLibrarySortType): NFTLibraryFilters =
    copy(sortType = if (this.sortType == sortType) NFTLibrarySortType.None else sortType)