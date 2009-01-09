//TODO 5: Put a progress bar beside feed item which is downloading
// Confirmation for export local opml
//TODO 5: Search/Add feed; if already exists, skip it. Check by Uri/Title.
//TODO 5: Resume does not use/remember correct item. Should player be used instead of resume button? Naaa
//TODO 4: Separate videoplayer
//TODO 5: Remeber feed item and go back to that one after resume activity for info, player....
//TODO 5: Refresh all feeds, do reread from db, clear cache, of metadata
// Play_list.xml should be fill parent so icons are right aligned
// Info should have all info on scrollable view, not just description.
// Unknown icon in downloaded view for feeditems without one
//TODO 5: Clicking a feeditem should display a menu for that item. Pressing menu should display a menu with options for all items.
//TODO 5: Longpress may start default action in feed item list. Own handler with shorter long press.
//TODO 4: Create play notification icon orange-right-arrow.
//TODO 4: save playlists
//TODO 6: Bug: Media player in locked loop consuming resources
//TODO 5: Broadcast instead of callback interface?
//TODO 5: Preview in feed list of latest feed item title.
// Keep phone alive when downloading. System tools - prevent phone from sleeping
//TODO 5: Switch between pubdate and asc title order in feed list.
//TODO 4: Menu option to mark feed item as read
//TODO 5: Setting: only auto queue downloaded
//TODO 5: Setting: only auto queue completed
//TODO 5: Playlist feed icon and status icon
//TODO 5: Queue item in playlist from feed item list options menu
//TODO 6: Add status icon in downloaded list view.
//TODO 6: Media button should only trigger acast and not music player, or consume intent.
//TODO 6: Library icon and text buttons.
//TODO 6: No mp3uri/downoad icon for text only feed items.
//TODO 6: Download latest from every feed
// Export OPML to sdcard/acast/acast.opml
//TODO 6: Playlist, queue, view, remove, reorder.
//TODO 5: Sort by date, unsorted, title, size, ...
//TODO 6: Remove notification when opening download list
//TODO 5: Move resume button to list so it is possible to move it out of screen like iphone.
//TODO 6: Search through all titles for all feeds
//TODO 6: Show feed icon for downloaded and downloadqueue
//TODO 5: sort on pubDate, title, ... (settings)
//TODO 6: Option menu should be for actions which affect all feeds/items. LongPress (ContectMenu?) should affect single items
//TODO 6: Increase size of feed/feeditem list rows
//TODO 5: dont show/send all download progress msgs
//TODO 5: Download estimated time
//TODO 6: Progress dialog should show title instead of url
//TODO 5: Add indeterminate status progress bar and loading on separate thread, during play init, since it takes time...
//TODO 6: Bug: audio notification triggered player does not stop audio
//TODO 6: Bug: info for feed item is not shown when opening player with notifiction
//TODO 6: Download queue not visible after close. How to go to file download page? Main page menu item.
//TODO 6: Remove from queue
//TODO 6: Download complete notification, whole queue or each download?
//TODO 6: Queue system
//TODO 6: list all downloaded together, let user play from list
//TODO 5: Longpress to open menu for feed/feeditem
//TODO 5: Bug: show error when feed can not be added
//TODO 5: Bug: Downloads can not handle screen timeout, when screen locks audio stops
//TODO 5: ContentProvider, MIME (text/xml) association, should trigger intent for acast. Update android manifest.
//      <intent-filter>
//      	<action android:name="android.intent.action.VIEW" />
//      	<action android:name="android.intent.action.BROWSABLE" />
//      	<category android:name="android.intent.category.DEFAULT" />
//      	<data android:mimeType="audio/x-scpls" />
//      	<data android:mimeType="audio/x-mpegurl" />
//      </intent-filter>
//TODO 5: What happens if feed already exist? Show dialog...
//TODO 5: Show activity name in title
//TODO 5: Settings dialog: 
//       auto delete, 
//       auto refresh all feeds at spec time (hourly, daily, week, month), 
//       auto download. 
//       Only download through wifi. 
//       Resume partly downloaded. 
//       Auto delete after playing
//       Auto-play next unplayed Episode when an Episode has finished playing.
//TODO 5: Other podcatchers: Dogg Catcher, NPE, Podcaster?, PodWeasel
//TODO 5: On multidownload, press (drag?) to activate/setcurrent download of podcast.
//TODO 5: Pause when incoming call, resume afterwards. Pause playback when a call is initiated. 
//TODO 5: Support video
//TODO 5: Pause/play with button on headset. Multi press; one: pause/play, two: seek forward, three: seek backward, long: ?
//TODO 5: Show partial downloads
//TODO 5: System tool to prevent phone from sleeping?
//TODO 5: Show if streaming when playing
//TODO 5: Use toast more. Toast.makeText().show()!!
//TODO 5: Show if new podcasts are available in feed list.
//TODO 5: Show if any podcasts is ongoing in feed list.
//TODO 5: Multiple Player activites, singleinstance? (launchMode="singleInstance" in the manifest file)
//TODO 5: Show new items that are newer then old update time.
//TODO 5: Ability to exit the application
//TODO 5: Error by notification from services, download/media
//TODO 5: Text size for feed/feeditem lists
//TODO 5: Low: Bug: mediaplayer prepare can not handle streaming mp3 url with redirect, http://podtrac.com/pts/redirect.mp3?http://www.48days.bullakaclients.com/12-17-08.mp3  
//TODO 5: Low: Show info on right side of screen during landscape mode?
//TODO 5: Low: Bug: MediaPlayer thread is started and never really released?
//TODO 5: Low: rotate with gravity or keyboard?
//TODO 5: Low: All Download progress screen. Queue downloads, show download queue and current progress. notification on completion/queue empty
//TODO 5: Low: show kb/s for download progress window
//TODO 5: Low: Username/password protected RSS
//TODO 5: Low: Use default media player with broadcast sender.
// Move "Add item" next to importOPML in search view.
// Lazy fetch feeds in feed list
// Date in feed item view
// Search in search results. Display Search button after a result is presented.
// Loading on refresh all feeds, show status bar processing circle
// Remove information from player, can be shown with menu->information.
// remove feed/feeditem confirmation
// Import through OPML
// Select/Search podcasts from podgrove.com, podcastalley.com?, digitalpodcast.com
// Play/Pause button, Bigger buttons, black and white? Use styled ic_media_play/pause?
// Refresh feed should not clear -downloads-/bookmark/completed
// Show file size and actual file size
// Thread RSS parsing and html get to awoid wait/force kill dialog.
// When lock screen and enable screen, Player controls are not working. 
// LOW: Download all feed items in sequence (if not already downloaded)
// Change notification icon to main(RSS) icon (to same as main icon?)
// Add icon to menu items
// On remove feed, remove physical files
// Pause mp on start to avoid short sound before first setPosition
// Bigger letters in feed list.
// Bug: Fix threaded mediaplayer bug which does not stop playback sometimes. 
// Individual bookmark
// Illegal character in path at index 37: http://192.168.254.125/audio/music/14 - Nowhere.mp3
// fetch size from rss xml
// show duration, seekbar duration update
// use file size with download progress bar
// use duration with player progress bar
// volume slider? No, use hard volume controller
// Show in (not)downloaded icons if feed item has ever completed
// http://192.168.254.125/mp3.xml does not work. mount -o bind problem...
// show if downloaded with icon
// Show in (not)downloaded icons if feed item has bookmark or not
// Player, press home, start acast = problem. Prob because of continuing duration progress thread
// Error when selecting add feed textview and switching to landscape mode
// resume last item button, show title of feed item
// store mp3s on sdcard
// escape chars in title before creating file name
// play in background, show notification in tray. Service, NotificationManager
// Use NotificationManager to be able to go back to current playing feed item
// (not used) AlarmManager, Context.getSystemService(Context.ALARM_SERVICE)
// Remove notification on pause and stop
// Draw main icon
// End service and remove notification when song is complete
// Fetch icon from RSS and present in feed list 
// Bug: Player, change screen mode, media restarts from last save point
// create /sdcard/<feed> dirs for mp3s
// Feed and feeditem extended information Dialog (or show everything in listitem)
// Increase size of start icon...
// Edit feed does not work. Remove?
// detailMessage	"Only the original thread that created a view hierarchy can touch its views." (id=830057026112); use handler
// E/MediaPlayer(  277): Attempt to call getDuration without a valid mediaplayer

/** Description
 * It is also possible to use ACast as a RSS feed reader.
 */
package com.mathias.android.acast;

public interface Constants {

	public static final String FEED = "feed";
	public static final String FEEDID = "feedid";
	public static final String FEEDITEM = "feeditem";
	public static final String FEEDITEMS = "feeditems";
	public static final String FEEDITEMID = "feeditemid";

	public static final int NOTIFICATION_DOWNLOADSERVICE_ID = R.id.add;
	public static final int NOTIFICATION_MEDIASERVICE_ID = R.id.addresult;

}
