/*
TODO 5: Interface to podnova.com
TODO 5: Introduction screen..., where to add feeds and info regarding players.
TODO 4: Bug: downloadedView -> queue item; does not add item at the end of queue.
TODO 5: Three progressHandler... for DownloadQueueList seems wrong. Is one added for each download?
TODO 6: notification player
TODO 6: All feed items view, including not downloaded
TODO 5: delete downloaded which are X days or older
TODO 5: problem when opening/removing sdcard
TODO 5: notification during download. progressbar for notification.
TODO 5: 'downloading' icon in status bar and for each feeditem during download.
TODO 4: Log Runtime.freeMemory() in thread preiodically.
TODO 5: DDMS-Heap-View never broke the 16MB barrier
TODO 5: check process memory consumption
TODO 5: OutOfMemory; bitmap... Problem in BitmapFactory.decodeFile()
	FeedList downlaod bug:
	onserviceconnected, DownloadService
	49280-byte external allocation too large for this process.
	VM won't let us allocate 49280 bytes
	Sutting down VM
	OOM: bitmap size exceeds VM budget
	at android.graphics.Bitmap.nativeCreate(Native Method)
	at android.graphics.Bitmap.createBitmap(Bitmap.java:343)
	From ListView
	Does bitmap-map store too much data?
	for (all of my big images) {
	     Bitmap b = decode(...);
	     canvas.drawBitmap(b, ...);
	     b.recycle();
	     // yikes, don't reference b again)
	}
TODO 6: Put a progress bar beside feed item which is downloading, or change color icon for queued items
TODO 5: Search/Add feed; if already exists, skip it. Check by Title (which is unique).
TODO 4: Separate videoplayer
TODO 5: Refresh all feeds, do reread from db, clear cache, of metadata
TODO 4: save playlists
TODO 4: Broadcast instead of callback interface?
TODO 4: Switch between pubdate and asc title order in feed list.
TODO 4: Menu option to mark feed item as read
TODO 5: Settings dialog: 
	Setting: auto delete, 
	Setting: auto refresh all feeds at spec time (hourly, daily, week, month), 
	Setting: auto download. 
	Setting: Only download through wifi. 
	Setting: Resume partly downloaded. 
	Setting: Auto delete after playing
	Setting: Auto-play next unplayed Episode when an Episode has finished playing.
	Setting: Full screen; remove activity title bar (requestWindowFeature(Window.FEATURE_NO_TITLE)), fullscreen (getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); ).
	Setting: auto play player onResume
	Setting: boolean use big button player or play list player as default
	Setting: only auto queue downloaded
	Setting: only auto queue completed
	Setting: Sort by date, unsorted, title, size, ... (settings?)
TODO 5: Playlist feed icon and status icon
TODO 5: Move resume button playlist to list so it is possible to move it out of screen like iphone.
TODO 6: Search through all titles for all feeds
TODO 5: Download estimated time
TODO 5: ContentProvider, MIME (text/xml) association, should trigger intent for acast. Update android manifest.
      <intent-filter>
      	<action android:name="android.intent.action.VIEW" />
      	<action android:name="android.intent.action.BROWSABLE" />
      	<category android:name="android.intent.category.DEFAULT" />
      	<data android:mimeType="audio/x-scpls" />
      	<data android:mimeType="audio/x-mpegurl" />
      </intent-filter>
TODO 5: On multidownload(queue), press (drag?) to activate/setcurrent download of podcast.
TODO 5: Pause when incoming call, resume afterwards. Pause playback when a call is initiated. 
TODO 6: Pause/play with button on headset. Multi press; one: pause/play, two: seek forward, three: seek backward, long: ? Media button should only trigger acast and not music player, or consume intent.
TODO 5: Show partial downloads
TODO 5: Show if streaming when playing
TODO 4: Ability to exit the application
TODO 5: Low: Bug: mediaplayer prepare can not handle streaming mp3 url with redirect, http://podtrac.com/pts/redirect.mp3?http://www.48days.bullakaclients.com/12-17-08.mp3  
TODO 5: Low: show kb/s for download progress window
TODO 5: Low: Username/password protected RSS
TODO 5: Low: Use default media player with broadcast sender.
Multiple Player activites, singleinstance? (launchMode="singleInstance" in the manifest file). Solved with Activity.finish()
Show new items that are newer then old update time. Use latest not touched instead. Otherwise have to handle multiple refresh...
Error by notification from services, download/media
Text size for feed/feeditem lists
Low: Show info on right side of screen during landscape mode?
Low: Bug: MediaPlayer thread is started and never really released?
Low: rotate with gravity or keyboard? Keyboard
Low: All Download progress screen. Queue downloads, show download queue and current progress. notification on completion/queue empty
Other podcatchers: Dogg Catcher, NPE, Podcaster?, PodWeasel
System tool to prevent phone from sleeping?
Use toast more. Toast.makeText().show()!!
Show if new podcasts are available in feed list.
Show if any podcasts is ongoing in feed list.
What happens if feed already exist? Show dialog...
Show activity name in title
Confirmation for export local opml
Resume does not use/remember correct item. Should player be used instead of resume button? Yes
Remeber feed item and go back to that one after resume activity for info, player....
Long clicking a feeditem should display a menu for that item. Pressing menu should display a menu with options for all items.
Create play notification icon orange-right-arrow.
Bug: Media player in locked loop consuming resources
Preview in feed list of latest feed item title.
Queue item in playlist from feed item list options menu
Add status icon in downloaded list view.
Library icon and text buttons.
No mp3uri/downoad icon for text only feed items.
Download latest from every feed
Playlist, queue, view, remove, reorder.
Remove notification when opening download list
Show feed icon for downloaded and downloadqueue
Increase size of feed/feeditem list rows
dont show/send all download progress msgs
Progress dialog should show title instead of url
Add indeterminate status progress bar and loading on separate thread, during play init, since it takes time...
Bug: audio notification triggered player does not stop audio
Bug: info for feed item is not shown when opening player with notifiction
Download queue not visible after close. How to go to file download page? Main page menu item.
Remove from queue
Download complete notification, whole queue or each download?
Queue system
list all downloaded together, let user play from list
Longpress to open menu for feed/feeditem
Bug: show error when feed can not be added
Bug: Downloads can not handle screen timeout, when screen locks audio stops
Light notification on download complete and refresh complete.
Remove empty categories when parsing RSS XML.
On notification intend downloaded, scroll and selected latest download item
feed item list -> feed item, scroll and select feed. By feed id in extras bundle?
Bug: Player and PlayList keeps on geting current pos
Remove resume and use player instead.
Read icon and rezize it to icon size befor put in map... icons max 48x48
Change playlist progressbar to seekbar
Handle failed download/timeout
Maximum icon size: x*y. Scale down during after download to sd.
Refresh all feeds does not work
show error during import
Play_list.xml should be fill parent so icons are right aligned
Info should have all info on scrollable view, not just description.
Unknown icon in downloaded view for feeditems without one
Keep phone alive when downloading. System tools - prevent phone from sleeping
Export OPML to sdcard/acast/acast.opml
Move "Add item" next to importOPML in search view.
Lazy fetch feeds in feed list
Date in feed item view
Search in search results. Display Search button after a result is presented.
Loading on refresh all feeds, show status bar processing circle
Remove information from player, can be shown with menu->information.
remove feed/feeditem confirmation
Import through OPML
Select/Search podcasts from podgrove.com, podcastalley.com?, digitalpodcast.com
Play/Pause button, Bigger buttons, black and white? Use styled ic_media_play/pause?
Refresh feed should not clear -downloads-/bookmark/completed
Show file size and actual file size
Thread RSS parsing and html get to awoid wait/force kill dialog.
When lock screen and enable screen, Player controls are not working. 
LOW: Download all feed items in sequence (if not already downloaded)
Change notification icon to main(RSS) icon (to same as main icon?)
Add icon to menu items
On remove feed, remove physical files
Pause mp on start to avoid short sound before first setPosition
Bigger letters in feed list.
Bug: Fix threaded mediaplayer bug which does not stop playback sometimes. 
Individual bookmark
Illegal character in path at index 37: http://192.168.254.125/audio/music/14 - Nowhere.mp3
fetch size from rss xml
show duration, seekbar duration update
use file size with download progress bar
use duration with player progress bar
volume slider? No, use hard volume controller
Show in (not)downloaded icons if feed item has ever completed
http://192.168.254.125/mp3.xml does not work. mount -o bind problem...
show if downloaded with icon
Show in (not)downloaded icons if feed item has bookmark or not
Player, press home, start acast = problem. Prob because of continuing duration progress thread
Error when selecting add feed textview and switching to landscape mode
resume last item button, show title of feed item
store mp3s on sdcard
escape chars in title before creating file name
play in background, show notification in tray. Service, NotificationManager
Use NotificationManager to be able to go back to current playing feed item
(not used) AlarmManager, Context.getSystemService(Context.ALARM_SERVICE)
Remove notification on pause and stop
Draw main icon
End service and remove notification when song is complete
Fetch icon from RSS and present in feed list 
Bug: Player, change screen mode, media restarts from last save point
create /sdcard/<feed> dirs for mp3s
Feed and feeditem extended information Dialog (or show everything in listitem)
Increase size of start icon...
Edit feed does not work. Remove?
detailMessage	"Only the original thread that created a view hierarchy can touch its views." (id=830057026112); use handler
E/MediaPlayer(  277): Attempt to call getDuration without a valid mediaplayer
*/
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

	public static final int NOTIFICATION_MEDIASERVICE_ID = R.id.author;
	public static final int NOTIFICATION_DOWNLOADING_ID = R.id.add;
	public static final int NOTIFICATION_DOWNLOADCOMPLETE_ID = R.id.addresult;
	public static final int NOTIFICATION_UPDATING_ID = R.id.updatedate;
	public static final int NOTIFICATION_UPDATECOMPLETE_ID = R.id.bookmark;

	public static final long INVALID_ID = -1;

}
