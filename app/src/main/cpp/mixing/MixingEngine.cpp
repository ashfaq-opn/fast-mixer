//
// Created by asalehin on 9/9/20.
//

#include "MixingEngine.h"

#include <utility>

MixingEngine::~MixingEngine() {
    auto it = sourceMap.begin();
    while (it != sourceMap.end()) {
        delete it->second;
    }
    sourceMap.clear();
}

void MixingEngine::addFile(string filePath) {
    auto it = sourceMap.find(filePath);
    if (it != sourceMap.end()) {
        filePath.erase();
        return;
    }
    FileDataSource* source = mixingIO.readFile(filePath);
    sourceMap.insert(pair<string, FileDataSource*>(filePath, source));
    filePath.erase();
}

unique_ptr<buffer_data> MixingEngine::readSamples(string filePath, size_t numSamples) {
    auto it = sourceMap.find(filePath);
    filePath.erase();
    if (it == sourceMap.end()) {
        buffer_data emptyData {
                .ptr = nullptr,
                .numSamples = 0
        };
        return make_unique<buffer_data>(emptyData);
    }
    return it->second->readData(numSamples);
}

void MixingEngine::deleteFile(string filePath) {
    auto it = sourceMap.find(filePath);
    if (it != sourceMap.end()) {
        delete it->second;
        sourceMap.erase(filePath);
    }
    filePath.erase();
}

int64_t MixingEngine::getAudioFileTotalSamples(string filePath) {
    auto it = sourceMap.find(filePath);
    if (it == sourceMap.end()) {
        return 0;
    }
    if (it->second == nullptr) {
        return 0;
    }
    filePath.erase();
    return it->second->getSampleSize();
}

