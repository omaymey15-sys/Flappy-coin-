<?xml version="1.0" encoding="utf-8"?>  <FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"  
android:layout_width="match_parent"  
android:layout_height="match_parent"  
android:background="#000000">

<!-- GameView (SurfaceView) -->  
<View  
    android:id="@+id/gameView"  
    android:layout_width="match_parent"  
    android:layout_height="match_parent" />  

<!-- Stats en jeu (en haut) -->  
<LinearLayout  
    android:layout_width="match_parent"  
    android:layout_height="80dp"  
    android:orientation="horizontal"  
    android:padding="12dp"  
    android:gravity="center_vertical">  

    <!-- Gauche: Score -->  
    <LinearLayout  
        android:layout_width="0dp"  
        android:layout_height="wrap_content"  
        android:layout_weight="1"  
        android:orientation="vertical"  
        android:background="#2a2a3e"  
        android:padding="8dp">  

        <TextView  
            android:text="Score"  
            android:textColor="#FFFFFF"  
            android:textSize="12sp"  
            android:layout_width="match_parent"  
            android:layout_height="wrap_content" />  

        <TextView  
            android:id="@+id/tvGameScore"  
            android:text="0"  
            android:textColor="#FFD700"  
            android:textSize="24sp"  
            android:textStyle="bold"  
            android:layout_width="match_parent"  
            android:layout_height="wrap_content" />  

    </LinearLayout>  

    <!-- Milieu: Distance & Temps -->  
    <LinearLayout  
        android:layout_width="0dp"  
        android:layout_height="wrap_content"  
        android:layout_weight="1"  
        android:orientation="vertical"  
        android:background="#2a2a3e"  
        android:padding="8dp"  
        android:layout_marginStart="8dp"  
        android:layout_marginEnd="8dp">  

        <TextView  
            android:id="@+id/tvDistance"  
            android:text="0m"  
            android:textColor="#FFFFFF"  
            android:textSize="12sp"  
            android:layout_width="match_parent"  
            android:layout_height="wrap_content" />  

        <TextView  
            android:id="@+id/tvTime"  
            android:text="00:00"  
            android:textColor="#FFFFFF"  
            android:textSize="12sp"  
            android:layout_width="match_parent"  
            android:layout_height="wrap_content" />  

    </LinearLayout>  

    <!-- Droite: Coins -->  
    <LinearLayout  
        android:layout_width="0dp"  
        android:layout_height="wrap_content"  
        android:layout_weight="1"  
        android:orientation="vertical"  
        android:background="#2a2a3e"  
        android:padding="8dp"  
        android:gravity="center">  

        <TextView  
            android:text="🪙"  
            android:textSize="24sp"  
            android:gravity="center"  
            android:layout_width="match_parent"  
            android:layout_height="wrap_content" />  

        <TextView  
            android:id="@+id/tvGameCoins"  
            android:text="0"  
            android:textColor="#FFD700"  
            android:textSize="18sp"  
            android:textStyle="bold"  
            android:gravity="center"  
            android:layout_width="match_parent"  
            android:layout_height="wrap_content" />  

    </LinearLayout>  

</LinearLayout>

</FrameLayout>