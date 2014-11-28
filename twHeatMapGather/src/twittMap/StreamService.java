package twittMap;

import twitter4j.*;

public class StreamService {
	/*
	 * public static void main(String[] args) throws Exception { StreamService
	 * ser = StreamService.getInstance(); ser.gatherNum(50, 5000);
	 * Thread.sleep(10000); ser.stopGather(); }
	 */

	TwitterStream twitterStream = null;
	DBManager dbM = null;
	private static final StreamService streamService = new StreamService();
	private StreamService() {
		/*
		 * ConfigurationBuilder cb = new ConfigurationBuilder();
		 * cb.setDebugEnabled(true);
		 * cb.setOAuthConsumerKey("EEWt5bXGCf4SLJmChlH8OUCSK");
		 * cb.setOAuthConsumerSecret
		 * ("wIrtkcS4RCvfqO3JapDtJZQpxwjrdJI7zAHsw0DUnBy6pIYz40");
		 * cb.setOAuthAccessToken
		 * ("191944365-to6zo7gowMWmNAqzhPwi0CXoQJ7p4t8CVVz33ht3");
		 * cb.setOAuthAccessTokenSecret
		 * ("i1uoX2ZEmLvB4Y7dQ31CaLxgJhRopm2EUahhYzPThWcxy"); twitterStream =
		 * new TwitterStreamFactory(cb.build()).getInstance();
		 */
		twitterStream = new TwitterStreamFactory().getInstance();
		dbM = new DBManager();
		SimpleQueueService.IniateSimpleQueueService();
		
		SNS.init();
		
		System.out.println("twitterStream " + twitterStream.toString()
				+ "dbM: " + dbM.toString());
	}

	public static StreamService getInstance() {
		return streamService;
	}

/*	// get the number of tweets with keyword
	public ArrayList<StreamStatus> getStream(String keyWord, int number)
			throws InterruptedException {
		TwitterStream twitterStream = new TwitterStreamFactory().getInstance();

		final ArrayList<StreamStatus> list = new ArrayList<StreamStatus>();
		StatusListener listener = new StatusListener() {
			@Override
			public void onStatus(Status status) {
				if (status.getGeoLocation() != null) {
					StreamStatus stramStatus = new StreamStatus();
					stramStatus.sId = status.getId();
					stramStatus.sName = status.getUser().getScreenName();
					stramStatus.sTime = status.getCreatedAt();
					stramStatus.sLatitude = status.getGeoLocation()
							.getLatitude();
					stramStatus.sLongitude = status.getGeoLocation()
							.getLongitude();
					stramStatus.sText = status.getText();
					System.out.println(stramStatus);
					list.add(stramStatus);
				}
			}

			@Override
			public void onDeletionNotice(
					StatusDeletionNotice statusDeletionNotice) {

			}

			@Override
			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
				System.out.println("Got track limitation notice:"
						+ numberOfLimitedStatuses);
			}

			@Override
			public void onScrubGeo(long userId, long upToStatusId) {
				System.out.println("Got scrub_geo event userId:" + userId
						+ " upToStatusId:" + upToStatusId);
			}

			@Override
			public void onStallWarning(StallWarning warning) {
				System.out.println("Got stall warning:" + warning);
			}

			@Override
			public void onException(Exception ex) {
				ex.printStackTrace();
			}
		};
		twitterStream.addListener(listener);
		// String[] keyword = { keyWord };
		// FilterQuery filtro = new FilterQuery().track(keyword);
		// twitterStream.filter(filtro);
		twitterStream.sample();
		while (list.size() < number) {
			Thread.yield();
		}

		twitterStream.cleanUp();
		twitterStream.shutdown();
		return list;
	}*/

	// main gather
	public void gatherNum(int num, int time) throws Exception {
		dbM.getDirver();
		dbM.connectAWS();
		final int total = num;
		final int sleepTime = time;

		try {
			StatusListener listener = new StatusListener() {
				int count = 0;

				@Override
				public void onStatus(Status status) {
					if (status.getGeoLocation() != null) {
						StreamStatus stramStatus = new StreamStatus();
						stramStatus.sId = status.getId();
						stramStatus.sName = status.getUser().getScreenName();
						stramStatus.sTime = status.getCreatedAt();
						stramStatus.sLatitude = status.getGeoLocation()
								.getLatitude();
						stramStatus.sLongitude = status.getGeoLocation()
								.getLongitude();
						stramStatus.sText = status.getText();
						dbM.update(stramStatus);
						// System.out.println("time:" + status.getCreatedAt());
						SimpleQueueService.SendMsg(stramStatus.sId,stramStatus.sText);
						if (total >= 0) {
							count++;
						}
						if (count == total) {
							count = 0;
							try {
								Thread.sleep(sleepTime);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						// System.out.println("count:" + count);
					}
				}

				@Override
				public void onDeletionNotice(
						StatusDeletionNotice statusDeletionNotice) {

				}

				@Override
				public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
					System.out.println("Got track limitation notice:"
							+ numberOfLimitedStatuses);
				}

				@Override
				public void onScrubGeo(long userId, long upToStatusId) {
					System.out.println("Got scrub_geo event userId:" + userId
							+ " upToStatusId:" + upToStatusId);
				}

				@Override
				public void onStallWarning(StallWarning warning) {
					System.out.println("Got stall warning:" + warning);
				}

				@Override
				public void onException(Exception ex) {
					ex.printStackTrace();
				}
			};
			twitterStream.addListener(listener);
			System.out.println("Start!!!");
			// twitterStream.sample();
			FilterQuery filter = new FilterQuery();
			double[][] boundingBox = { { -180, -90 }, { 180, 90 } };
			filter.locations(boundingBox);
			twitterStream.filter(filter);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void gather() {
		dbM.getDirver();
		dbM.connectAWS();
		SimpleQueueService.IniateSimpleQueueService();
		try {
			StatusListener listener = new StatusListener() {
				@Override
				public void onStatus(Status status) {
					if (status.getGeoLocation() != null) {
						StreamStatus stramStatus = new StreamStatus();
						stramStatus.sId = status.getId();
						stramStatus.sName = status.getUser().getScreenName();
						stramStatus.sTime = status.getCreatedAt();
						stramStatus.sLatitude = status.getGeoLocation()
								.getLatitude();
						stramStatus.sLongitude = status.getGeoLocation()
								.getLongitude();
						stramStatus.sText = status.getText();
						dbM.update(stramStatus);
						SimpleQueueService.SendMsg(stramStatus.sId,stramStatus.sText);
					}
				}

				@Override
				public void onDeletionNotice(
						StatusDeletionNotice statusDeletionNotice) {

				}

				@Override
				public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
					System.out.println("Got track limitation notice:"
							+ numberOfLimitedStatuses);
				}

				@Override
				public void onScrubGeo(long userId, long upToStatusId) {
					System.out.println("Got scrub_geo event userId:" + userId
							+ " upToStatusId:" + upToStatusId);
				}

				@Override
				public void onStallWarning(StallWarning warning) {
					System.out.println("Got stall warning:" + warning);
				}

				@Override
				public void onException(Exception ex) {
					ex.printStackTrace();
				}
			};
			twitterStream.addListener(listener);
			System.out.println("Start!!!");
			// twitterStream.sample();
			FilterQuery filter = new FilterQuery();
			double[][] boundingBox = { { -180, -90 }, { 180, 90 } };
			filter.locations(boundingBox);
			twitterStream.filter(filter);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stopGather() {
		twitterStream.cleanUp();
		twitterStream.clearListeners();
		twitterStream.shutdown();
		System.out.println("twitterStream: " + twitterStream.toString());
		try {
			Thread.sleep(1000);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			dbM.shutdown();
		}
		// System.out.println("dbM: " + dbM.toString());
		try {
			SimpleQueueService.DeleteSimpleQueueService();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Stop!!!");
	}
	// public static void beginGather(int i) {
	// if(i == 1) gather();
	// else gather()
	// }
}
