/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef FAST_MIXER_AUDIOSOURCE_H
#define FAST_MIXER_AUDIOSOURCE_H

#include <cstdint>
#include "../Constants.h"

class DataSource {
public:
    virtual ~DataSource(){};
    virtual int64_t getSize() const = 0;
    virtual void setPlayHead(int64_t playHead) = 0;
    virtual int64_t getPlayHead() = 0;
    virtual float getAbsMaxSampleValue() const = 0;
    virtual float getMaxSampleValue() const = 0;
    virtual float getMinSampleValue() const = 0;
    virtual AudioProperties getProperties() const  = 0;
    virtual const float* getData() const = 0;
    virtual int64_t getSelectionStart() = 0;
    virtual int64_t getSelectionEnd() = 0;
};


#endif //RHYTHMGAME_AUDIOSOURCE_H
