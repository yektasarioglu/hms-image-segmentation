package com.yektasarioglu.imagesegmentation.ui

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.yektasarioglu.imagesegmentation.R

import com.yektasarioglu.imagesegmentation.databinding.FragmentImageSegmentationListBinding
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnNeverAskAgain
import permissions.dispatcher.OnPermissionDenied

import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class ImageSegmentationListFragment : Fragment() {

    private var binding: FragmentImageSegmentationListBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) : View? {
        binding = FragmentImageSegmentationListBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.staticHumanBodySegmentationButton?.setOnClickListener { navigateToStaticHumanBodySegmentationFragment() }
        binding?.streamHumanBodySegmentationButton?.setOnClickListener { navigateToStreamHumanBodySegmentationFragmentWithPermissionCheck() }
    }

    fun navigateToStaticHumanBodySegmentationFragment() = findNavController().navigate(ImageSegmentationListFragmentDirections.actionImageSegmentationListFragmentToStaticHumanBodySegmentationFragment())

    @NeedsPermission(Manifest.permission.CAMERA)
    fun navigateToStreamHumanBodySegmentationFragment() = findNavController().navigate(ImageSegmentationListFragmentDirections.actionImageSegmentationListFragmentToStreamHumanBodySegmentationFragment())

    @OnPermissionDenied(Manifest.permission.CAMERA)
    fun onCameraDenied() {
        Toast.makeText(activity, getString(R.string.camera_permission_denied), Toast.LENGTH_SHORT).show()
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    fun onCameraNeverAskAgain() {
        Toast.makeText(activity, getString(R.string.enable_permissions_manually), Toast.LENGTH_SHORT).show()
    }

}