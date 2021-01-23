//
// Created by asalehin on 7/9/20.
//

#ifndef FAST_MIXER_RECORDINGENGINE_H
#define FAST_MIXER_RECORDINGENGINE_H

#ifndef MODULE_NAME
#define MODULE_NAME "AudioEngine"
#endif

#include <oboe/Definitions.h>
#include <oboe/AudioStream.h>
#include "../logging_macros.h"
#include "../SourceMapStore.h"
#include "RecordingIO.h"
#include "streams/RecordingStream.h"
#include "streams/LivePlaybackStream.h"
#include "streams/PlaybackStream.h"

class RecordingEngine {

public:

    RecordingEngine(string appDir, string recordingSessionId, bool recordingScreenViewModelPassed);
    ~RecordingEngine();

    void startRecording();
    void stopRecording();

    void startLivePlayback();
    void stopLivePlayback();

    bool startPlayback();
    bool startMixingTracksPlayback();
    void stopMixingTracksPlayback();
    void stopAndResetPlayback();
    void pausePlayback();

    void flushWriteBuffer();
    void restartPlayback();

    int getCurrentMax();

    void resetCurrentMax();

    void setStopPlayback();

    int getTotalRecordedFrames();

    int getCurrentPlaybackProgress();

    void setPlayHead(int position);

    int getDurationInSeconds();

    void resetAudioEngine();

    void closePlaybackStream();

    void stopPlaybackCallable();

    bool startPlaybackCallable();

    bool startMixingTracksPlaybackCallable();

    void addSourcesToPlayer(string* strArr, int count);

private:

    const char* TAG = "Recording Engine:: %s";

    string mRecordingSessionId = nullptr;
    string mAppDir = nullptr;
    bool mPlayback = true;

    int bakPlayHead = 0;

    mutex recordingStreamMtx;
    mutex livePlaybackStreamMtx;
    mutex playbackStreamMtx;

    SourceMapStore* mSourceMapStore;
    RecordingIO mRecordingIO;

    shared_ptr<FileDataSource> mDataSource {nullptr};

    RecordingStream recordingStream = RecordingStream(&mRecordingIO);
    LivePlaybackStream livePlaybackStream = LivePlaybackStream(&mRecordingIO);
    PlaybackStream playbackStream = PlaybackStream(&mRecordingIO);
    bool mRecordingScreenViewModelPassed = false;

    void stopPlayback();
};


#endif //FAST_MIXER_RECORDINGENGINE_H
