package team.mino.core.designsystem.foundation.icons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.foundation.icons.icons.AiReview
import team.mino.core.designsystem.foundation.icons.icons.ArrowDown
import team.mino.core.designsystem.foundation.icons.icons.ArrowDownThick
import team.mino.core.designsystem.foundation.icons.icons.ArrowLeft
import team.mino.core.designsystem.foundation.icons.icons.ArrowLeftThick
import team.mino.core.designsystem.foundation.icons.icons.ArrowRight
import team.mino.core.designsystem.foundation.icons.icons.ArrowRightThick
import team.mino.core.designsystem.foundation.icons.icons.ArrowUp
import team.mino.core.designsystem.foundation.icons.icons.ArrowUpThick
import team.mino.core.designsystem.foundation.icons.icons.Bell
import team.mino.core.designsystem.foundation.icons.icons.BellFill
import team.mino.core.designsystem.foundation.icons.icons.Blank
import team.mino.core.designsystem.foundation.icons.icons.Bookmark
import team.mino.core.designsystem.foundation.icons.icons.BookmarkFill
import team.mino.core.designsystem.foundation.icons.icons.Bubble
import team.mino.core.designsystem.foundation.icons.icons.BubbleFill
import team.mino.core.designsystem.foundation.icons.icons.CaretDown
import team.mino.core.designsystem.foundation.icons.icons.CaretUp
import team.mino.core.designsystem.foundation.icons.icons.Check
import team.mino.core.designsystem.foundation.icons.icons.CheckThick
import team.mino.core.designsystem.foundation.icons.icons.ChevronLeft
import team.mino.core.designsystem.foundation.icons.icons.ChevronRight
import team.mino.core.designsystem.foundation.icons.icons.CircleCheck
import team.mino.core.designsystem.foundation.icons.icons.CircleCheckFill
import team.mino.core.designsystem.foundation.icons.icons.CircleExclamation
import team.mino.core.designsystem.foundation.icons.icons.CircleExclamationFill
import team.mino.core.designsystem.foundation.icons.icons.CircleInfo
import team.mino.core.designsystem.foundation.icons.icons.CircleInfoFill
import team.mino.core.designsystem.foundation.icons.icons.CircleQuestion
import team.mino.core.designsystem.foundation.icons.icons.CircleQuestionFill
import team.mino.core.designsystem.foundation.icons.icons.Close
import team.mino.core.designsystem.foundation.icons.icons.CloseThick
import team.mino.core.designsystem.foundation.icons.icons.Coffee
import team.mino.core.designsystem.foundation.icons.icons.CoffeeFill
import team.mino.core.designsystem.foundation.icons.icons.Compass
import team.mino.core.designsystem.foundation.icons.icons.CompassFill
import team.mino.core.designsystem.foundation.icons.icons.Copy
import team.mino.core.designsystem.foundation.icons.icons.Document
import team.mino.core.designsystem.foundation.icons.icons.DocumentFill
import team.mino.core.designsystem.foundation.icons.icons.DocumentText
import team.mino.core.designsystem.foundation.icons.icons.DocumentTextFill
import team.mino.core.designsystem.foundation.icons.icons.Download
import team.mino.core.designsystem.foundation.icons.icons.ExternalLink
import team.mino.core.designsystem.foundation.icons.icons.Eye
import team.mino.core.designsystem.foundation.icons.icons.EyeFill
import team.mino.core.designsystem.foundation.icons.icons.EyeSlash
import team.mino.core.designsystem.foundation.icons.icons.EyeSlashFill
import team.mino.core.designsystem.foundation.icons.icons.FaceSmile
import team.mino.core.designsystem.foundation.icons.icons.FaceSmileFill
import team.mino.core.designsystem.foundation.icons.icons.Folder
import team.mino.core.designsystem.foundation.icons.icons.FolderFill
import team.mino.core.designsystem.foundation.icons.icons.History
import team.mino.core.designsystem.foundation.icons.icons.Home
import team.mino.core.designsystem.foundation.icons.icons.HomeFill
import team.mino.core.designsystem.foundation.icons.icons.Image
import team.mino.core.designsystem.foundation.icons.icons.Inbox
import team.mino.core.designsystem.foundation.icons.icons.Keyboard
import team.mino.core.designsystem.foundation.icons.icons.Link
import team.mino.core.designsystem.foundation.icons.icons.List
import team.mino.core.designsystem.foundation.icons.icons.ListCategory
import team.mino.core.designsystem.foundation.icons.icons.ListOrdered
import team.mino.core.designsystem.foundation.icons.icons.Location
import team.mino.core.designsystem.foundation.icons.icons.LocationFill
import team.mino.core.designsystem.foundation.icons.icons.LogoApple
import team.mino.core.designsystem.foundation.icons.icons.LogoBrunch
import team.mino.core.designsystem.foundation.icons.icons.LogoFacebook
import team.mino.core.designsystem.foundation.icons.icons.LogoGooglePlay
import team.mino.core.designsystem.foundation.icons.icons.LogoInstagram
import team.mino.core.designsystem.foundation.icons.icons.LogoKakao
import team.mino.core.designsystem.foundation.icons.icons.LogoLinkedIn
import team.mino.core.designsystem.foundation.icons.icons.LogoMicrosoft
import team.mino.core.designsystem.foundation.icons.icons.LogoNaverBlog
import team.mino.core.designsystem.foundation.icons.icons.LogoX
import team.mino.core.designsystem.foundation.icons.icons.LogoYoutube
import team.mino.core.designsystem.foundation.icons.icons.Mail
import team.mino.core.designsystem.foundation.icons.icons.MailOpen
import team.mino.core.designsystem.foundation.icons.icons.Menu
import team.mino.core.designsystem.foundation.icons.icons.MenuThick
import team.mino.core.designsystem.foundation.icons.icons.MoreHorizontal
import team.mino.core.designsystem.foundation.icons.icons.MoreVertical
import team.mino.core.designsystem.foundation.icons.icons.MoreVerticalTight
import team.mino.core.designsystem.foundation.icons.icons.MyLocation
import team.mino.core.designsystem.foundation.icons.icons.Pencil
import team.mino.core.designsystem.foundation.icons.icons.PencilFill
import team.mino.core.designsystem.foundation.icons.icons.Person
import team.mino.core.designsystem.foundation.icons.icons.PersonFill
import team.mino.core.designsystem.foundation.icons.icons.PersonPlus
import team.mino.core.designsystem.foundation.icons.icons.PersonPlusFill
import team.mino.core.designsystem.foundation.icons.icons.Persons
import team.mino.core.designsystem.foundation.icons.icons.PersonsFill
import team.mino.core.designsystem.foundation.icons.icons.Phone
import team.mino.core.designsystem.foundation.icons.icons.PhoneFill
import team.mino.core.designsystem.foundation.icons.icons.Pin
import team.mino.core.designsystem.foundation.icons.icons.PinFill
import team.mino.core.designsystem.foundation.icons.icons.Plus
import team.mino.core.designsystem.foundation.icons.icons.PlusThick
import team.mino.core.designsystem.foundation.icons.icons.Refresh
import team.mino.core.designsystem.foundation.icons.icons.Reset
import team.mino.core.designsystem.foundation.icons.icons.Search
import team.mino.core.designsystem.foundation.icons.icons.SearchThick
import team.mino.core.designsystem.foundation.icons.icons.Send
import team.mino.core.designsystem.foundation.icons.icons.SendFill
import team.mino.core.designsystem.foundation.icons.icons.Setting
import team.mino.core.designsystem.foundation.icons.icons.Share
import team.mino.core.designsystem.foundation.icons.icons.ShareIos
import team.mino.core.designsystem.foundation.icons.icons.SquareCaret
import team.mino.core.designsystem.foundation.icons.icons.SquareCheck
import team.mino.core.designsystem.foundation.icons.icons.SquarePlus
import team.mino.core.designsystem.foundation.icons.icons.SquarePlusFill
import team.mino.core.designsystem.foundation.icons.icons.Star
import team.mino.core.designsystem.foundation.icons.icons.StarFill
import team.mino.core.designsystem.foundation.icons.icons.Sun
import team.mino.core.designsystem.foundation.icons.icons.Thumbnail
import team.mino.core.designsystem.foundation.icons.icons.Trash
import team.mino.core.designsystem.foundation.icons.icons.Tune
import team.mino.core.designsystem.foundation.icons.icons.Upload
import team.mino.core.designsystem.foundation.icons.icons.Write
import team.mino.core.designsystem.foundation.typography.token.TypographyAccessKeyToken
import team.mino.core.designsystem.foundation.typography.token.value
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.util.preview.UiModePreviews

@Composable
private fun IconCatalogItem(
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
        Text(
            text = icon.name.substringAfterLast('.'),
            style = TypographyAccessKeyToken.Caption1Regular.value.copy(
                color = ColorAccessKeyToken.LabelNeutral.value,
            ),
            textAlign = TextAlign.Center,
        )
    }
}

@UiModePreviews
@Composable
private fun MinoIconsPreview() {
    MinoAndroidAppTheme {
        Column(
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            iconCatalog.chunked(4).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    row.forEach { icon ->
                        IconCatalogItem(
                            icon = icon,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

private val iconCatalog: List<ImageVector> by lazy {
    listOf(
        MinoIcons.AiReview,
        MinoIcons.ArrowDown,
        MinoIcons.ArrowDownThick,
        MinoIcons.ArrowLeft,
        MinoIcons.ArrowLeftThick,
        MinoIcons.ArrowRight,
        MinoIcons.ArrowRightThick,
        MinoIcons.ArrowUp,
        MinoIcons.ArrowUpThick,
        MinoIcons.Bell,
        MinoIcons.BellFill,
        MinoIcons.Blank,
        MinoIcons.Bookmark,
        MinoIcons.BookmarkFill,
        MinoIcons.Bubble,
        MinoIcons.BubbleFill,
        MinoIcons.CaretDown,
        MinoIcons.CaretUp,
        MinoIcons.Check,
        MinoIcons.CheckThick,
        MinoIcons.ChevronLeft,
        MinoIcons.ChevronRight,
        MinoIcons.CircleCheck,
        MinoIcons.CircleCheckFill,
        MinoIcons.CircleExclamation,
        MinoIcons.CircleExclamationFill,
        MinoIcons.CircleInfo,
        MinoIcons.CircleInfoFill,
        MinoIcons.CircleQuestion,
        MinoIcons.CircleQuestionFill,
        MinoIcons.Close,
        MinoIcons.CloseThick,
        MinoIcons.Coffee,
        MinoIcons.CoffeeFill,
        MinoIcons.Compass,
        MinoIcons.CompassFill,
        MinoIcons.Copy,
        MinoIcons.Document,
        MinoIcons.DocumentFill,
        MinoIcons.DocumentText,
        MinoIcons.DocumentTextFill,
        MinoIcons.Download,
        MinoIcons.ExternalLink,
        MinoIcons.Eye,
        MinoIcons.EyeFill,
        MinoIcons.EyeSlash,
        MinoIcons.EyeSlashFill,
        MinoIcons.FaceSmile,
        MinoIcons.FaceSmileFill,
        MinoIcons.Folder,
        MinoIcons.FolderFill,
        MinoIcons.History,
        MinoIcons.Home,
        MinoIcons.HomeFill,
        MinoIcons.Image,
        MinoIcons.Inbox,
        MinoIcons.Keyboard,
        MinoIcons.Link,
        MinoIcons.List,
        MinoIcons.ListCategory,
        MinoIcons.ListOrdered,
        MinoIcons.Location,
        MinoIcons.LocationFill,
        MinoIcons.LogoApple,
        MinoIcons.LogoBrunch,
        MinoIcons.LogoFacebook,
        MinoIcons.LogoGooglePlay,
        MinoIcons.LogoInstagram,
        MinoIcons.LogoKakao,
        MinoIcons.LogoLinkedIn,
        MinoIcons.LogoMicrosoft,
        MinoIcons.LogoNaverBlog,
        MinoIcons.LogoX,
        MinoIcons.LogoYoutube,
        MinoIcons.Mail,
        MinoIcons.MailOpen,
        MinoIcons.Menu,
        MinoIcons.MenuThick,
        MinoIcons.MoreHorizontal,
        MinoIcons.MoreVertical,
        MinoIcons.MoreVerticalTight,
        MinoIcons.MyLocation,
        MinoIcons.Pencil,
        MinoIcons.PencilFill,
        MinoIcons.Person,
        MinoIcons.PersonFill,
        MinoIcons.PersonPlus,
        MinoIcons.PersonPlusFill,
        MinoIcons.Persons,
        MinoIcons.PersonsFill,
        MinoIcons.Phone,
        MinoIcons.PhoneFill,
        MinoIcons.Pin,
        MinoIcons.PinFill,
        MinoIcons.Plus,
        MinoIcons.PlusThick,
        MinoIcons.Refresh,
        MinoIcons.Reset,
        MinoIcons.Search,
        MinoIcons.SearchThick,
        MinoIcons.Send,
        MinoIcons.SendFill,
        MinoIcons.Setting,
        MinoIcons.Share,
        MinoIcons.ShareIos,
        MinoIcons.SquareCaret,
        MinoIcons.SquareCheck,
        MinoIcons.SquarePlus,
        MinoIcons.SquarePlusFill,
        MinoIcons.Star,
        MinoIcons.StarFill,
        MinoIcons.Sun,
        MinoIcons.Thumbnail,
        MinoIcons.Trash,
        MinoIcons.Tune,
        MinoIcons.Upload,
        MinoIcons.Write,
    )
}
