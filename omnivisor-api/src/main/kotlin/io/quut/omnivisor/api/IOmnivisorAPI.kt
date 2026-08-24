package io.quut.omnivisor.api

interface IOmnivisorAPI
{
	val omnivisor: IOmnivisor

	companion object
	{
		private var instance: IOmnivisorAPI? = null

		@JvmStatic
		fun get(): IOmnivisorAPI = this.instance ?: throw IllegalStateException("Omnivisor is not initialized")

		fun register(instance: IOmnivisorAPI)
		{
			if (this.instance != null)
			{
				throw IllegalStateException("Already registered")
			}

			this.instance = instance
		}

		fun unregister(instance: IOmnivisorAPI)
		{
			if (this.instance != instance)
			{
				throw IllegalArgumentException("Mismatched instance")
			}

			this.instance = null
		}
	}
}
