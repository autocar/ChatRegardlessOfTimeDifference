<?php
/**
 * Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

# [START speech_quickstart]
# Includes the autoloader for libraries installed with composer
require __DIR__ . '/vendor/autoload.php';

# Imports the Google Cloud client library
use Google\Cloud\Speech\SpeechClient;
use Google\Cloud\Storage\StorageClient;
use Google\Cloud\Speech\Connection\ConnectionInterface;
use Google\Cloud\Speech\Connection\Rest;

# Your Google Cloud Platform project ID
$projectId = 'chat-166522';

if (isset($_GET["URL"])) {

    # Instantiates a client
    $speech = new SpeechClient([
        'projectId' => $projectId,
        'languageCode' => 'en-US',
    ]);

    # The name of the audio file to transcribe
    //$fileName = __DIR__ . '/resources/audio.raw';
    //$fileAddr = 'https://firebasestorage.googleapis.com/v0/b/chat-f976b.appspot.com/o/audios%2Faudio.raw?alt=media&token=30e00f6f-00d9-475e-a082-ff085b744693';
    $fileAddr = $_GET["URL"];
    # The audio file's encoding and sample rate
    $options = [
        'encoding' => 'LINEAR16',
        'sampleRateHertz' => 16000,
    ];

    # Detects speech in the audio file
    $results = $speech->recognize(fopen($fileAddr, 'r'), $options);

    echo 'Transcription: ' . $results[0]['transcript'];
    # [END speech_quickstart]
    return $results;
}
