package com.mrikso.anitube.app.utils;

public class ProfileBackgroundImages {
    static String[] images = new String[] {
        "https://pixabay.com/get/g7affa7923d09bfc7bb379e554db0f127facd947a43c6c603bd846e018dcb5b0665ad5ee31a8c5f3c7d8222ce6aa8cf3c815a259fcb7683030f79d1a6924cb516019dc31b0e1f1213c6bc63095be32045_1920.jpg",
        "https://pixabay.com/get/g91fe96080f7e8fc9cffa32f2e57d4235b284385501a8011ed0eac95fc9d9ed06b335680a9fefaaef16adb2599b8a4d146ff4cf187e0f19060943d86080db8b926b997026cb9475939aaf3ade56948e65_1280.jpg",
        "https://pixabay.com/get/gda56110d40dc38c2a04f10bd79e7620eb065bdec9135ad94e43212b366f4dbf2e9cd305d9894d48e83d8905a9471dad5718ff14c66d22a629b79d541a11c9446444b36022ac87d36c5c66c4c8f7a4d6d_1920.jpg",
        "https://pixabay.com/get/g37a0750ab4c6b3f1c5627d43057b514601a7efacf7ed1fb7aee6443915d9a49a10c97fd0fb2c45cc224f5fe78062c825082f47fdfa3a1d2accbf83b96ce41d49c8e15efd4f90edea70ed2ce0661890f8_1280.jpg",
        "https://pixabay.com/get/gd41748e31ec3b51657656e26c5ac2ab7549320f5499e7444e4e99a388b69d8ed4d00782932c135901e681a0e6c117cc620313118bb7a13289601f3d466b16d657dead72f2cc6a959270a85d93d34cb77_1280.jpg"
    };

    public static String getImage() {

        return images[getRandomNumber(0, images.length)];
    }

    public static int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }
}
